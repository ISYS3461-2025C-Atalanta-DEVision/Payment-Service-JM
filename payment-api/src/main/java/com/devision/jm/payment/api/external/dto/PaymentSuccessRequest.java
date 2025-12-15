package com.devision.jm.payment.api.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment Success Request
 *
 * Request body for POST /api/payments/success endpoint.
 * Used to simulate a successful payment and trigger Kafka event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessRequest {

    /**
     * User ID (the profile's userId)
     */
    private String userId;

    /**
     * Plan type (e.g., "PREMIUM")
     */
    private String planType;
}
