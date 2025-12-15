package com.devision.jm.payment.controller;

import com.devision.jm.payment.api.external.dto.PaymentSuccessRequest;
import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;
import com.devision.jm.payment.api.internal.interfaces.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentExternalApi paymentExternalApi;
  private final KafkaProducerService kafkaProducerService;

  public PaymentController(PaymentExternalApi paymentExternalApi, KafkaProducerService kafkaProducerService) {
    this.paymentExternalApi = paymentExternalApi;
    this.kafkaProducerService = kafkaProducerService;
  }

  @PostMapping("/checkout")
  public ResponseEntity<StripeResponse> checkout(@RequestBody SubscriptionRequest request) {
    return ResponseEntity.ok(paymentExternalApi.checkout(request));
  }

  /**
   * Payment Success Endpoint
   *
   * Simulates a successful payment and publishes a Kafka event
   * to notify Profile Service to upgrade the user to PREMIUM.
   *
   * POST /api/payments/success
   * Body: { "userId": "...", "planType": "PREMIUM" }
   */
  @PostMapping("/success")
  public ResponseEntity<String> paymentSuccess(@RequestBody PaymentSuccessRequest request) {
    log.info("Received payment success request for userId: {}, planType: {}",
            request.getUserId(), request.getPlanType());

    // Create and publish the payment completed event
    PaymentCompletedEvent event = PaymentCompletedEvent.builder()
            .userId(request.getUserId())
            .planType(request.getPlanType())
            .paidAt(LocalDateTime.now())
            .build();

    kafkaProducerService.publishPaymentCompletedEvent(event);

    return ResponseEntity.ok("Payment success event published for userId: " + request.getUserId());
  }
}
