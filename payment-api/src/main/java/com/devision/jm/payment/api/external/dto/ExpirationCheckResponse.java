package com.devision.jm.payment.api.external.dto;

public class ExpirationCheckResponse {

    private int expiringSoonCount;
    private int expiredCount;

    public ExpirationCheckResponse() {
    }

    public ExpirationCheckResponse(int expiringSoonCount, int expiredCount) {
        this.expiringSoonCount = expiringSoonCount;
        this.expiredCount = expiredCount;
    }

    public int getExpiringSoonCount() {
        return expiringSoonCount;
    }

    public int getExpiredCount() {
        return expiredCount;
    }
}
