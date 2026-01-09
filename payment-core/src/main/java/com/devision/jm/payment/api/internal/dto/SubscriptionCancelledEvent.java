package com.devision.jm.payment.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Subscription Cancelled Event
 *
 * Published to Kafka topic "subscription-cancelled" when a subscription is cancelled.
 * Consumed by Profile Service to downgrade user subscription to FREE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCancelledEvent {

    /**
     * User ID (from Auth/Profile Service)
     * This is the userId that Profile Service uses to find the profile
     */
    private String userId;

    /**
     * Company ID (for company subscriptions)
     */
    private String companyId;

    /**
     * Applicant ID (for applicant subscriptions)
     */
    private String applicantId;

    /**
     * Previous plan type (PREMIUM)
     */
    private String previousPlanType;

    /**
     * Timestamp when subscription was cancelled
     */
    private LocalDateTime cancelledAt;
}
