package com.devision.jm.payment.service.impl;

import org.springframework.stereotype.Service;

import com.devision.jm.payment.repository.TransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.devision.jm.payment.model.enums.TransactionStatus;
import org.springframework.beans.factory.annotation.Value;

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

    if ("checkout.session.completed".equals(event.getType())) {
      Session session = (Session) event.getDataObjectDeserializer()
        .getObject()
        .orElse(null);

      if (session != null) {
        String sessionId = session.getId();
        String subscriptionId = session.getSubscription();

        transactionRepository.findByStripeCheckoutSessionId(sessionId)
          .ifPresent(tx -> {
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setSubscriptionId(subscriptionId);
            transactionRepository.save(tx);
          });
      }
    }

    return "ok";
  }

}
