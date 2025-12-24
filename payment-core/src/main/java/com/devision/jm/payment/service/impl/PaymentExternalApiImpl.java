package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.PaymentSuccessRequest;
import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;
import com.devision.jm.payment.api.internal.interfaces.KafkaProducerService;
import com.devision.jm.payment.exception.PaymentException;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentExternalApiImpl implements PaymentExternalApi {

  private final StripeService stripeService;
  private final KafkaProducerService kafkaProducerService;

  public PaymentExternalApiImpl(StripeService stripeService, KafkaProducerService kafkaProducerService) {
    this.stripeService = stripeService;
    this.kafkaProducerService = kafkaProducerService;
  }

  @Override
  public StripeResponse checkout(SubscriptionRequest request) {
    try {
      return stripeService.checkoutPayment(request);
    } catch (StripeException e) {
      throw new PaymentException("Stripe checkout failed", e);
    }
  }

  @Override
  public String processPaymentSuccess(PaymentSuccessRequest request) {
    log.info("Processing payment success for userId: {}, planType: {}",
            request.getUserId(), request.getPlanType());

    // Create internal DTO and publish to Kafka
    PaymentCompletedEvent event = PaymentCompletedEvent.builder()
            .userId(request.getUserId())
            .planType(request.getPlanType())
            .paidAt(LocalDateTime.now())
            .build();

    kafkaProducerService.publishPaymentCompletedEvent(event);

    return "Payment success event published for userId: " + request.getUserId();
  }
}
