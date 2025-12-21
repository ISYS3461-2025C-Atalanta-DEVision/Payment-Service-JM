package com.devision.jm.payment.api.internal.dto;

import java.util.UUID;

//result for business flow
public class CheckoutSessionResult {

    private final String checkoutUrl;
    private final String sessionId;
    private final String transactionId;

    public CheckoutSessionResult(String checkoutUrl, String sessionId, String transactionId) {
        this.checkoutUrl = checkoutUrl;
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

}
