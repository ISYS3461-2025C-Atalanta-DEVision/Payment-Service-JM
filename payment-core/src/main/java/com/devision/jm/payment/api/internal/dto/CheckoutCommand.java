package com.devision.jm.payment.api.internal.dto;
import java.util.UUID;

public class CheckoutCommand {
    private UUID companyId;
    private UUID applicantId;
    private String payerEmail;
    private String planType;
    private String currency;

    public CheckoutCommand(UUID companyId, UUID applicantId, String payerEmail, String planType, String currency) {
        this.companyId = companyId;
        this.applicantId = applicantId;
        this.payerEmail = payerEmail;
        this.planType = planType;
        this.currency = currency;
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