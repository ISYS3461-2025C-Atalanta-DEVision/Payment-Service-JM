package com.devision.jm.payment.consumer;

import com.devision.jm.payment.api.internal.dto.SyncApplicantEvent;
import com.devision.jm.payment.model.entity.Subscription;
import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Kafka Consumer for Sync Applicant Events
 *
 * Listens to JA's applicant lifecycle events (create/update/delete).
 * Handles subscription cleanup when applicants are deleted.
 *
 * Topic: sync.applicant
 *
 * Flow:
 * 1. JA publishes sync.applicant event when applicant is deleted
 * 2. This consumer receives the event
 * 3. For deletion: cancels Stripe subscription and sets status to CANCELLED
 * 4. This prevents ENDING_SOON and ENDED events from being sent to deleted applicants
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
@Transactional(rollbackFor = Exception.class)
public class SyncApplicantEventConsumer {

    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${stripe.secret-key:}")
    private String stripeApiKey;

    /**
     * Consume sync.applicant events from JA
     *
     * Topic: sync.applicant
     * Group: payment-service-group
     */
    @KafkaListener(
            topics = "sync.applicant",
            groupId = "${spring.kafka.consumer.group-id:payment-service-group}",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    public void consumeSyncApplicantEvent(String message) {
        log.info("Received sync.applicant event");

        try {
            SyncApplicantEvent event = objectMapper.readValue(message, SyncApplicantEvent.class);

            log.info("Processing sync.applicant event: type={}, applicantId={}",
                    event.getEventType(), event.getApplicantId());

            // Validate required fields
            if (event.getApplicantId() == null || event.getApplicantId().isBlank()) {
                log.warn("Applicant ID is null or blank, skipping event");
                return;
            }

            // Only handle deletion events
            if (event.isDeleted()) {
                log.info("Processing applicant deletion for subscription cleanup: {}",
                        event.getApplicantId());
                processApplicantDeletion(event.getApplicantId());
            } else {
                log.debug("Non-deletion event received ({}), no subscription action required",
                        event.getEventType());
            }

        } catch (Exception e) {
            log.error("Failed to process sync.applicant event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process sync.applicant event", e);
        }
    }

    /**
     * Process applicant deletion - cancel subscription
     */
    private void processApplicantDeletion(String applicantId) {
        // Find active subscription for this applicant
        Optional<Subscription> activeOpt = subscriptionRepository
                .findFirstByApplicantIdAndStatusOrderByEndDateDesc(applicantId, SubscriptionStatus.ACTIVE);

        if (activeOpt.isEmpty()) {
            log.info("No active subscription found for deleted applicant: {}", applicantId);
            return;
        }

        Subscription subscription = activeOpt.get();

        // Cancel Stripe subscription if exists
        cancelStripeSubscription(subscription);

        // Set status to CANCELLED
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndDate(LocalDate.now());
        subscriptionRepository.save(subscription);

        log.info("Successfully cancelled subscription for deleted applicant. applicantId={}, subscriptionId={}, stripeSubId={}",
                applicantId, subscription.getId(), subscription.getStripeSubscriptionId());
    }

    /**
     * Cancel Stripe subscription to stop billing
     */
    private void cancelStripeSubscription(Subscription subscription) {
        String stripeSubId = subscription.getStripeSubscriptionId();

        if (stripeSubId == null || stripeSubId.isBlank()) {
            log.warn("Subscription {} has no Stripe subscription ID, skipping Stripe cancellation",
                    subscription.getId());
            return;
        }

        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            log.warn("Stripe API key not configured, skipping Stripe cancellation for subscription {}",
                    subscription.getId());
            return;
        }

        try {
            Stripe.apiKey = stripeApiKey;

            com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.retrieve(stripeSubId);
            stripeSub.cancel();

            log.info("Cancelled Stripe subscription: {}", stripeSubId);

        } catch (Exception e) {
            // Log error but don't fail - subscription status will still be set to CANCELLED
            log.error("Failed to cancel Stripe subscription {}: {}", stripeSubId, e.getMessage());
        }
    }
}
