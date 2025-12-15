package com.devision.jm.payment.api.external.dto;

import java.util.UUID;


public class SubscriptionRequest {

    private UUID companyId;
    private UUID applicantId;
    private String payerEmail;
    private String planType;      // BASIC / PREMIUM
    private String currency;

    public SubscriptionRequest() {
    }
    public UUID getCompanyId() {
        return companyId;
    }
    public UUID getApplicantId() {
        return applicantId;
    }
    public String getPayerEmail() {
        return payerEmail;
    }
    public String getPlanType() {
        return planType;
    }
    public String getCurrency() {
        return currency;
    }
}
