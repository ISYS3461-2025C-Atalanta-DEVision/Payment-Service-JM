package com.devision.jm.payment.api.external.dto;

public class SubscriptionResponse {

    private String subscriptionId;
    private String status;
    private String startDate;
    private String endDate;

    public SubscriptionResponse() {
    }

    public SubscriptionResponse(String subscriptionId, String status, String startDate, String endDate) {
        this.subscriptionId = subscriptionId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
