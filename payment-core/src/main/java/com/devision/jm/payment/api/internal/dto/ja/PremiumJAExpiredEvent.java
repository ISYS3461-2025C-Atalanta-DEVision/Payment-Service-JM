package com.devision.jm.payment.api.internal.dto.ja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an applicant's PREMIUM subscription expires.
 * Topic: subscription.premium.ja.expired
 * Consumer: JA team services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumJAExpiredEvent {

    private String applicantId;

    private String subscriptionId;

    /**
     * Expiration timestamp in ISO format (e.g., "2026-02-10T00:00:00")
     */
    private String expiredAt;
}
