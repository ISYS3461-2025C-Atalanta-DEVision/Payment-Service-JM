package com.devision.jm.payment.controller;

import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    private final PaymentExternalApi paymentExternalApi;

    public StripeWebhookController(PaymentExternalApi paymentExternalApi) {
        this.paymentExternalApi = paymentExternalApi;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        String result = paymentExternalApi.handleStripeWebhook(payload, sigHeader);

        if ("invalid signature".equals(result)) {
            return ResponseEntity.status(400).body("Invalid signature");
        }
        return ResponseEntity.ok("ok");
    }
}
