package com.devision.jm.payment.api.internal.dto;

public class CreateSubscriptionResult {

    private String clientSecret;
    private String stripeSubscriptionId;
    private String stripePaymentIntentId;
    private String transactionId;

    public CreateSubscriptionResult(String clientSecret, String stripeSubscriptionId, String stripePaymentIntentId, String transactionId) {
        this.clientSecret = clientSecret;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.transactionId = transactionId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
