package com.devision.jm.payment.api.external.interfaces;

import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;

public interface PaymentExternalApi {

    SubscriptionIntentResponse createSubscriptionIntent(SubscriptionRequest request);

    String handleStripeWebhook(String payload, String stripeSignature);
}
