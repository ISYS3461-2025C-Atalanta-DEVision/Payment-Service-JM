package com.devision.jm.payment.api.internal.dto.ja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an applicant cancels their PREMIUM subscription.
 * Topic: subscription.premium.ja.closed
 * Consumer: JA team services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumJAClosedEvent {

    private String applicantId;

    private String subscriptionId;

    /**
     * Cancellation timestamp in ISO format (e.g., "2026-01-15T10:30:00")
     */
    private String closedAt;
}
