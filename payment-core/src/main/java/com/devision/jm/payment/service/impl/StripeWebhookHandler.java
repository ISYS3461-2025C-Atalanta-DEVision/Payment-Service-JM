package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.model.enums.TransactionStatus;
import com.devision.jm.payment.repository.TransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookHandler {

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final TransactionRepository transactionRepository;

    public StripeWebhookHandler(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public String handleWebhook(String payload, String stripeSignature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, endpointSecret);
        } catch (SignatureVerificationException e) {
            return "invalid signature";
        }

        String type = event.getType();

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
