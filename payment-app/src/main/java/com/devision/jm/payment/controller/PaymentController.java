package com.devision.jm.payment.controller;

import com.devision.jm.payment.api.external.dto.PaymentSuccessRequest;
import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Payment Controller
 *
 * REST endpoints for payment operations.
 * Uses only external interfaces from payment-api module.
 * Internal DTOs and Kafka logic are encapsulated in payment-core.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentExternalApi paymentExternalApi;

  public PaymentController(PaymentExternalApi paymentExternalApi) {
    this.paymentExternalApi = paymentExternalApi;
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

    // Delegate to service layer (internal DTOs handled in payment-core)
    String result = paymentExternalApi.processPaymentSuccess(request);

    return ResponseEntity.ok(result);
  }
}
