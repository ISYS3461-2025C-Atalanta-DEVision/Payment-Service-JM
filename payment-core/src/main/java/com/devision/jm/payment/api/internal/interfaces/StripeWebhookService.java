package com.devision.jm.payment.api.internal.interfaces;

public interface StripeWebhookService {
    String handleWebhook(String payload, String stripeSignature);

}
