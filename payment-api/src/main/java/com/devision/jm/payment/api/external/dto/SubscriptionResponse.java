package com.devision.jm.payment.api.external.dto;

import java.util.UUID;

public class SubscriptionResponse {

    private UUID subscriptionId;
    private String status;
    private String startDate;
    private String endDate;

    public SubscriptionResponse(UUID subscriptionId, String status, String startDate, String endDate) {
        this.subscriptionId = subscriptionId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
