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

        String type = event.getType();

        // 1) update transaction
        if ("invoice.paid".equals(type)) {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice != null) {
                String stripeSubscriptionId = invoice.getSubscription();
                String paymentIntentId = invoice.getPaymentIntent();

                // update "latest" tx của subscription
                transactionRepository.findBySubscriptionIdOrderByCreatedAtDesc(stripeSubscriptionId)
                        .stream()
                        .findFirst()
                        .ifPresent(tx -> {
                            tx.setStatus(TransactionStatus.COMPLETED);
                            tx.setStripePaymentId(paymentIntentId);
                            transactionRepository.save(tx);
                        });

                // 2) update subscription entity in DB and publish Kafka event
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

                                // 3) Publish Kafka event to notify Profile Service
                                if (kafkaProducerService != null) {
                                    String userId = subEntity.getCompanyId() != null
                                            ? subEntity.getCompanyId().toString()
                                            : (subEntity.getApplicantId() != null
                                                    ? subEntity.getApplicantId().toString()
                                                    : null);

                                    if (userId != null) {
                                        PaymentCompletedEvent kafkaEvent = PaymentCompletedEvent.builder()
                                                .userId(userId)
                                                .planType(subEntity.getPlanType())
                                                .paidAt(LocalDateTime.now())
                                                .build();

                                        log.info("Publishing PaymentCompletedEvent for userId: {}, planType: {}",
                                                userId, subEntity.getPlanType());
                                        kafkaProducerService.publishPaymentCompletedEvent(kafkaEvent);
                                    } else {
                                        log.warn("Cannot publish Kafka event: no companyId or applicantId found for subscription {}",
                                                stripeSubscriptionId);
                                    }
                                } else {
                                    log.warn("KafkaProducerService not available, skipping Kafka event");
                                }

                            } catch (Exception e) {
                                log.error("Error processing invoice.paid webhook: {}", e.getMessage(), e);
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
