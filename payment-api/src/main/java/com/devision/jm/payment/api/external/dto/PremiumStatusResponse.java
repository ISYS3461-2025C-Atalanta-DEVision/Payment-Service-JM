package com.devision.jm.payment.api.external.dto;

import java.util.UUID;

public class PremiumStatusResponse {

    private UUID companyId;
    private boolean premium;
    private String status;
    private String endDate;

    public PremiumStatusResponse() {
    }

    public PremiumStatusResponse(UUID companyId, boolean premium, String status, String endDate) {
        this.companyId = companyId;
        this.premium = premium;
        this.status = status;
        this.endDate = endDate;
    }

    public UUID getCompanyId() {
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
