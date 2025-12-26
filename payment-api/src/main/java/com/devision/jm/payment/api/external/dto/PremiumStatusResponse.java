package com.devision.jm.payment.api.external.dto;

public class PremiumStatusResponse {

    private String companyId;
    private boolean premium;
    private String status;
    private String endDate;

    public PremiumStatusResponse() {
    }

    public PremiumStatusResponse(String companyId, boolean premium, String status, String endDate) {
        this.companyId = companyId;
        this.premium = premium;
        this.status = status;
        this.endDate = endDate;
    }

    public String getCompanyId() {
        return companyId;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getStatus() {
        return status;
    }

    public String getEndDate() {
        return endDate;
    }
}
