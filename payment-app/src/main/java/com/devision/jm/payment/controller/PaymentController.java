package com.devision.jm.payment.controller;

import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;

/*POST /api/payments/checkout
Called by your FE */

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
}
