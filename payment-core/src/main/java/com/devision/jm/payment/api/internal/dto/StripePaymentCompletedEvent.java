package com.devision.jm.payment.api.internal.dto;
import java.util.UUID;


public class StripePaymentCompletedEvent {
    private final String sessionId;
    private final String paymentIntentId;
    private final String customerId;
    private final String planType;
    private final UUID companyId;
    private final UUID applicantId;

    public StripePaymentCompletedEvent(String sessionId, String paymentIntentId, String customerId, String planType, UUID companyId, UUID applicantId) {
        this.sessionId = sessionId;
        this.paymentIntentId = paymentIntentId;
        this.customerId = customerId;
        this.planType = planType;
        this.companyId = companyId;
        this.applicantId = applicantId;
    }
    
}
