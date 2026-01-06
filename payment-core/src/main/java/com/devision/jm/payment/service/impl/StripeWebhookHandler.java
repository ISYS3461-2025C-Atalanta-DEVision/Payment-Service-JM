package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;
import com.devision.jm.payment.api.internal.interfaces.KafkaProducerService;
import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.model.enums.TransactionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import com.devision.jm.payment.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.StripeObject;

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

    private static final ObjectMapper MAPPER = new ObjectMapper();


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
      throw new IllegalArgumentException("Missing Stripe-Signature header");
    }
    if (endpointSecret == null || endpointSecret.isBlank()) {
      throw new IllegalArgumentException("Stripe webhook secret not configured");
    }

    final Event event;
    try {
      event = Webhook.constructEvent(payload, stripeSignature, endpointSecret);
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid Stripe signature");
    }

        String type = event.getType();

        // 1) update transaction
        if ("invoice.paid".equals(type)) {
            Invoice invoice = null;

            // 1) Try safe deserialization
            try {
                StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
                if (obj instanceof Invoice) {
                invoice = (Invoice) obj;
                }
            } catch (Exception e) {
                log.warn("invoice.paid safe deserialization failed: {}", e.getMessage());
            }

            // 2) Fallback: parse invoiceId from payload and retrieve from Stripe
            if (invoice == null) {
                String invoiceId = null;
                try {
                JsonNode root = MAPPER.readTree(payload);
                invoiceId = root.path("data").path("object").path("id").asText();
                if (invoiceId != null && invoiceId.isBlank()) invoiceId = null;
                } catch (Exception e) {
                log.warn("Cannot parse invoiceId from payload: {}", e.getMessage());
                }

                if (invoiceId == null) {
                log.warn("invoice.paid but invoice is null AND invoiceId not found in payload. eventId={}", event.getId());
                return "ok";
                }

                try {
                // make sure apiKey is set
                if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
                    com.stripe.Stripe.apiKey = stripeSecretKey;
                }
                invoice = Invoice.retrieve(invoiceId);
                log.info("Fetched invoice from Stripe API. invoiceId={}", invoiceId);
                } catch (Exception e) {
                log.error("Failed to retrieve invoice from Stripe. invoiceId={} err={}", invoiceId, e.getMessage(), e);
                return "ok";
                }
            }

            // Now invoice is guaranteed not null
            String stripeSubscriptionId = invoice.getSubscription();
            String paymentIntentId = invoice.getPaymentIntent();

            log.info("🧾 invoice.paid invoiceId={} stripeSubId={} paymentIntentId={}",
                invoice.getId(), stripeSubscriptionId, paymentIntentId);

            if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
                log.warn("invoice.paid but missing stripeSubscriptionId. invoiceId={}", invoice.getId());
                return "ok";
            }

            // Update transaction by stripeSubscriptionId
            transactionRepository.findFirstBySubscriptionIdOrderByCreatedAtDesc(stripeSubscriptionId)
                .ifPresentOrElse(tx -> {
                log.info("🎯 Found tx id={} oldStatus={}", tx.getId(), tx.getStatus());

                tx.setStatus(TransactionStatus.COMPLETED);
                if (paymentIntentId != null) tx.setStripePaymentId(paymentIntentId);
                transactionRepository.save(tx);

                log.info("✅ Updated tx COMPLETED id={}", tx.getId());
                }, () -> log.warn("❌ No tx found for stripeSubId={}", stripeSubscriptionId));

            // Update subscription ACTIVE
            subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .ifPresentOrElse(subEntity -> {
                subEntity.setStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(subEntity);

                log.info("✅ Updated subscription ACTIVE id={} stripeSubId={}",
                    subEntity.getId(), stripeSubscriptionId);
                }, () -> log.warn("❌ Subscription not found for stripeSubId={}", stripeSubscriptionId));

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
