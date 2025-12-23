package com.devision.jm.payment.controller;

import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.external.interfaces.ExpirationCheckResponse;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import com.devision.jm.payment.api.external.dto.PremiumStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentExternalApi paymentExternalApi;

    public PaymentController(PaymentExternalApi paymentExternalApi) {
        this.paymentExternalApi = paymentExternalApi;
    }

    // 1) Create subscription intent -> trả clientSecret
    @PostMapping("/subscriptions/intent")
    public ResponseEntity<SubscriptionIntentResponse> createSubscriptionIntent(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(paymentExternalApi.createSubscriptionIntent(request));
    }

    // 2) Get transaction by id
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentExternalApi.getTransactionById(transactionId));
    }

    // 3) Search transactions (optional but useful)
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> findTransactions(
            @RequestParam(required = false) String payerEmail,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID applicantId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(paymentExternalApi.findTransactions(payerEmail, companyId, applicantId, status));
    }

    // 4) Get subscription by internal id (UUID string)
    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable String subscriptionId) {
        return ResponseEntity.ok(paymentExternalApi.getSubscriptionById(subscriptionId));
    }

    // 5) Get subscription by Stripe id
    @GetMapping("/subscriptions/stripe/{stripeSubscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionByStripeId(@PathVariable String stripeSubscriptionId) {
        return ResponseEntity.ok(paymentExternalApi.getSubscriptionByStripeId(stripeSubscriptionId));
    }

    // 6) Premium status for company profile page
    @GetMapping("/premium/company/{companyId}")
    public ResponseEntity<PremiumStatusResponse> getCompanyPremiumStatus(@PathVariable UUID companyId) {
        return ResponseEntity.ok(paymentExternalApi.getCompanyPremiumStatus(companyId));
    }

    // 7) Cancel company subscription (optional)
    @PostMapping("/subscriptions/company/{companyId}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelCompanySubscription(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "true") boolean cancelAtPeriodEnd
    ) {
        return ResponseEntity.ok(paymentExternalApi.cancelCompanySubscription(companyId, cancelAtPeriodEnd));
    }

    // 8) Stripe webhook
    @PostMapping("/webhooks/stripe")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature", required = false) String stripeSignature
    ) {
        paymentExternalApi.handleStripeWebhook(payload, stripeSignature);
        return ResponseEntity.ok("ok");
    }

    // 9) Internal: manual trigger expiration check (for testing)
    @PostMapping("/internal/subscriptions/run-expiration-check")
    public ResponseEntity<ExpirationCheckResponse> runExpirationCheckNow() {
        return ResponseEntity.ok(paymentExternalApi.runExpirationCheckNow());
    }
}
