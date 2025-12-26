package com.devision.jm.payment.api.internal.dto;

public class CreateSubscriptionCommand {

    private String companyId;
    private String applicantId;
    private String payerEmail;
    private String planType;
    private String currency;

    public CreateSubscriptionCommand(String companyId, String applicantId, String payerEmail, String planType, String currency) {
        this.companyId = companyId;
        this.applicantId = applicantId;
        this.payerEmail = payerEmail;
        this.planType = planType;
        this.currency = currency;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getApplicantId() {
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
