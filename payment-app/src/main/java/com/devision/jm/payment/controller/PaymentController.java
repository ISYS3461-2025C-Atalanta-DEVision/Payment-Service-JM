package com.devision.jm.payment.controller;

import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentExternalApi paymentExternalApi;

    public PaymentController(PaymentExternalApi paymentExternalApi) {
        this.paymentExternalApi = paymentExternalApi;
    }

    // Option B: FE gọi để lấy client_secret (Stripe Elements)
    @PostMapping("/subscription/intent")
    public ResponseEntity<SubscriptionIntentResponse> createSubscriptionIntent(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(paymentExternalApi.createSubscriptionIntent(request));
    }

   
}
