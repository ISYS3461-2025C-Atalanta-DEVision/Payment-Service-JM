package com.devision.jm.payment.api.external.interfaces;

import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;

public interface PaymentExternalApi {
  StripeResponse checkout(SubscriptionRequest request);

    // Stripe webhook entrypoint (raw payload + signature header)
    String handleStripeWebhook(String payload, String stripeSignature);
  
}
