package com.devision.jm.payment.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payment Completed Event
 *
 * Published to Kafka topic "payment-completed" when a payment succeeds.
 * Consumed by Profile Service to upgrade user subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    /**
     * User ID (from Auth/Profile Service)
     * This is the userId that Profile Service uses to find the profile
     */
    private String userId;

    /**
     * Plan type (PREMIUM)
     */
    private String planType;

    /**
     * Timestamp when payment was completed
     */
    private LocalDateTime paidAt;
}
