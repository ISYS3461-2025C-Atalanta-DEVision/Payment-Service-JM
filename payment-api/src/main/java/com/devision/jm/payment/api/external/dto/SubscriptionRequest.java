package com.devision.jm.payment.api.external.dto;


public class SubscriptionRequest {

    private String companyId;
    private String applicantId;
    private String payerEmail;
    private String planType;      // PREMIUM
    private String currency;

    public SubscriptionRequest() {
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
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }
    public void setPlanType(String planType) {
        this.planType = planType;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
