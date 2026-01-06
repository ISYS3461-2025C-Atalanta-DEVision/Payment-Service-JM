package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;
import com.devision.jm.payment.api.internal.interfaces.KafkaProducerService;
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
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.devision.jm.payment.api.internal.interfaces.StripeWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripeWebhookHandler implements StripeWebhookService {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public StripeWebhookHandler(
            TransactionRepository transactionRepository,
            SubscriptionRepository subscriptionRepository,
            @Autowired(required = false) KafkaProducerService kafkaProducerService) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public String handleWebhook(String payload, String stripeSignature) {
        // Validate signature header exists
        if (stripeSignature == null || stripeSignature.isBlank()) {
            log.warn("Webhook received without Stripe-Signature header");
            throw new IllegalArgumentException("Missing Stripe-Signature header");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Stripe signature");
        }

        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            com.stripe.Stripe.apiKey = stripeSecretKey;
        }

        String type = event.getType();

        // 1) update transaction
        if ("invoice.paid".equals(type)) {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice == null) {
                log.warn("invoice.paid but invoice is null");
                return "ok";
            }

            String stripeSubscriptionId = invoice.getSubscription();
            String paymentIntentId = invoice.getPaymentIntent();

            log.info("🧾 invoice.paid invoiceId={} stripeSubId={} paymentIntent={}",
                    invoice.getId(), stripeSubscriptionId, paymentIntentId);

            if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
                log.warn("invoice.paid but missing subscription id. invoiceId={}", invoice.getId());
                return "ok";
            }

            // ✅ Update transaction by stripeSubscriptionId (most reliable with your DB design)
            transactionRepository.findFirstBySubscriptionIdOrderByCreatedAtDesc(stripeSubscriptionId)
                    .ifPresentOrElse(tx -> {
                        log.info("🎯 Found tx id={} currentStatus={}", tx.getId(), tx.getStatus());

                        tx.setStatus(TransactionStatus.COMPLETED);
                        if (paymentIntentId != null) tx.setStripePaymentId(paymentIntentId);
                        transactionRepository.save(tx);

                        log.info("💾 Updated tx COMPLETED id={}", tx.getId());
                    }, () -> log.warn("❌ No transaction found for stripeSubId={}", stripeSubscriptionId));

            // ✅ Update subscription entity to ACTIVE
            subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                    .ifPresentOrElse(subEntity -> {
                        subEntity.setStatus(SubscriptionStatus.ACTIVE);
                        subscriptionRepository.save(subEntity);

                        log.info("💾 Updated subscription ACTIVE id={} stripeSubId={}",
                                subEntity.getId(), stripeSubscriptionId);
                    }, () -> log.warn("❌ Subscription not found by stripeSubscriptionId={}", stripeSubscriptionId));

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

                LocalDate endDate = (sub.getCurrentPeriodEnd() != null)
                        ? Instant.ofEpochSecond(sub.getCurrentPeriodEnd())
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                        : LocalDate.now();

                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                        .ifPresent(subEntity -> {
                            subEntity.setStatus(SubscriptionStatus.CANCELLED);
                            subEntity.setEndDate(endDate);
                            subscriptionRepository.save(subEntity);
                        });
            }
            return "ok";
        }

        return "ok";
    }
}
