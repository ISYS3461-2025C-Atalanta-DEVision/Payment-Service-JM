package com.devision.jm.payment.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Deleted Event (Internal DTO)
 *
 * Kafka event consumed when a user/company is deleted by admin from Auth Service.
 * Payment Service uses this to cancel subscriptions and clean up billing.
 *
 * Topic: user-deleted
 * Producer: Auth Service
 *
 * Cleanup Actions:
 * - Cancel active Stripe subscription
 * - Set subscription status to CANCELLED
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {

    /**
     * User ID from Auth Service (MongoDB ObjectId as String)
     * This is the companyId used to find and cancel subscription
     */
    private String userId;

    /**
     * User's email - for audit/logging purposes
     */
    private String email;

    /**
     * Reason for deletion (optional)
     */
    private String reason;

    /**
     * Admin who performed the deletion (optional)
     */
    private String deletedBy;

    /**
     * Event timestamp
     */
    private LocalDateTime deletedAt;
}
