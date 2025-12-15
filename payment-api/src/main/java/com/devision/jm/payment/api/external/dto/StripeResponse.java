package com.devision.jm.payment.api.external.dto;


public class StripeResponse {
    private String checkoutUrl;
    private String sessionId;

    public StripeResponse(String checkoutUrl, String sessionId) {
        this.checkoutUrl = checkoutUrl;
        this.sessionId = sessionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getSessionId() {
        return sessionId;
    }


}
