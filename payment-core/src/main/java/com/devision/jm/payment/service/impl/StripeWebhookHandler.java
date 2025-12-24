package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.model.enums.TransactionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import com.devision.jm.payment.repository.TransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.devision.jm.payment.api.internal.interfaces.StripeWebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookHandler implements StripeWebhookService {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public StripeWebhookHandler(TransactionRepository transactionRepository, SubscriptionRepository subscriptionRepository) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public String handleWebhook(String payload, String stripeSignature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, endpointSecret);
        } catch (SignatureVerificationException e) {
            return "invalid signature";
        }

        String type = event.getType();

        // 1) update transaction
        if ("invoice.paid".equals(type)) {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice != null) {
                String stripeSubscriptionId = invoice.getSubscription();
                String paymentIntentId = invoice.getPaymentIntent();

                // update “latest” tx của subscription
                transactionRepository.findBySubscriptionIdOrderByCreatedAtDesc(stripeSubscriptionId)
                        .stream()
                        .findFirst()
                        .ifPresent(tx -> {
                            tx.setStatus(TransactionStatus.COMPLETED);
                            tx.setStripePaymentId(paymentIntentId);
                            transactionRepository.save(tx);
                        });

                // 2) update subscription entity in DB
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                        .ifPresent(subEntity -> {
                            try {
                                // fetch Stripe subscription to get period end
                                Subscription stripeSub = Subscription.retrieve(stripeSubscriptionId);

                                long periodStartSec = stripeSub.getCurrentPeriodStart() == null ? 0L : stripeSub.getCurrentPeriodStart();
                                long periodEndSec = stripeSub.getCurrentPeriodEnd() == null ? 0L : stripeSub.getCurrentPeriodEnd();

                                LocalDate startDate = periodStartSec == 0L
                                        ? LocalDate.now()
                                        : Instant.ofEpochSecond(periodStartSec).atZone(ZoneId.systemDefault()).toLocalDate();

                                LocalDate endDate = periodEndSec == 0L
                                        ? LocalDate.now().plusMonths(1)
                                        : Instant.ofEpochSecond(periodEndSec).atZone(ZoneId.systemDefault()).toLocalDate();

                                subEntity.setStatus(SubscriptionStatus.ACTIVE);
                                subEntity.setStartDate(startDate);
                                subEntity.setEndDate(endDate);
                                subEntity.setLastRenewedDate(LocalDate.now());

                                subscriptionRepository.save(subEntity);

                            } catch (Exception e) {
                                // log nếu cần, nhưng đừng throw để Stripe không retry vô hạn
                            }
                        });
            }
            return "ok";
        }

        if ("invoice.payment_failed".equals(type)) {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice != null) {
                String stripeSubscriptionId = invoice.getSubscription();
                transactionRepository.findBySubscriptionIdOrderByCreatedAtDesc(stripeSubscriptionId)
                        .stream()
                        .findFirst()
                        .ifPresent(tx -> {
                            tx.setStatus(TransactionStatus.FAILED);
                            transactionRepository.save(tx);
                        });

                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                        .ifPresent(sub -> {
                            sub.setStatus(SubscriptionStatus.EXPIRED); 
                            subscriptionRepository.save(sub);
                        });
            }
            return "ok";
        }

        if ("customer.subscription.deleted".equals(type)) {
            Subscription sub = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
            if (sub != null) {
                String stripeSubscriptionId = sub.getId();
                transactionRepository.findBySubscriptionIdOrderByCreatedAtDesc(stripeSubscriptionId)
                        .stream()
                        .findFirst()
                        .ifPresent(tx -> {
                            tx.setStatus(TransactionStatus.FAILED);
                            transactionRepository.save(tx);
                        });
            }
            return "ok";
        }

        return "ok";
    }
}
