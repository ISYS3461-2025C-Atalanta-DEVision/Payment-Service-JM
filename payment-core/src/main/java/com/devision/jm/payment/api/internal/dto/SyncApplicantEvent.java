package com.devision.jm.payment.api.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sync Applicant Event (Kafka DTO)
 *
 * Consumed from JA Service when applicants are deleted.
 * Used to cancel subscriptions when an applicant is removed.
 *
 * Topic: sync.applicant
 *
 * Example payload for deletion:
 * {
 *   "eventType": "deleted",
 *   "applicantId": "6965bb64889aa53ba441636a"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncApplicantEvent {

    /**
     * Event type: "created", "updated", or "deleted"
     */
    private String eventType;

    /**
     * Applicant ID from JA Service
     */
    private String applicantId;

    /**
     * Check if this is a deletion event
     */
    public boolean isDeleted() {
        return "deleted".equalsIgnoreCase(eventType);
    }
}
