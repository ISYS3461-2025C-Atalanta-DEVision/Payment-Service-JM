package com.devision.jm.payment.api.internal.dto.ja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an applicant upgrades to PREMIUM subscription.
 * Topic: subscription.premium.ja.created
 * Consumer: JA team services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumJACreatedEvent {

    private String applicantId;

    private String subscriptionId;

    /**
     * Subscription tier: "NORMAL" or "PREMIUM"
     */
    private String subscriptionTier;

    /**
     * Start date in ISO format (e.g., "2026-01-10")
     */
    private String startDate;

    /**
     * End date in ISO format (e.g., "2026-02-10")
     */
    private String endDate;
}
