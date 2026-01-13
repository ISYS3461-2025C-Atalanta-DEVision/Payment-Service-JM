package com.devision.jm.payment.consumer;

import com.devision.jm.payment.api.internal.dto.UserDeletedEvent;
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
 * Kafka Consumer for User Deleted Events
 *
 * Listens to the "user-deleted" topic from Auth Service.
 * Cleans up subscriptions when a company is deleted.
 * Only loaded when kafka.enabled=true.
 *
 * Microservice Architecture (A.3.2):
 * - Consumes events from Auth Service via Kafka
 *
 * Flow:
 * 1. Admin deletes company in Auth Service
 * 2. Auth Service publishes UserDeletedEvent to Kafka
 * 3. This consumer receives the event
 * 4. Cancels Stripe subscription (if active)
 * 5. Sets subscription status to CANCELLED
 *
 * Cleanup Actions:
 * - Cancel active Stripe subscription to stop billing
 * - Set subscription status to CANCELLED
 * - Preserve subscription record for audit/history
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
@Transactional(rollbackFor = Exception.class)
public class UserDeletedEventConsumer {

    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${stripe.secret-key:}")
    private String stripeApiKey;

    /**
     * Consume UserDeletedEvent from Kafka
     *
     * Topic: user-deleted
     * Group: payment-service-group
     */
    @KafkaListener(
            topics = "user-deleted",
            groupId = "${spring.kafka.consumer.group-id:payment-service-group}",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    public void consumeUserDeletedEvent(String message) {
        log.info("Received user-deleted event: {}", message);

        try {
            // Deserialize JSON message to DTO
            UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);

            log.info("Processing UserDeletedEvent for userId: {}, email: {}",
                    event.getUserId(), event.getEmail());

            String companyId = event.getUserId();

            // Find active subscription for this company
            Optional<Subscription> activeOpt = subscriptionRepository
                    .findFirstByCompanyIdAndStatusOrderByEndDateDesc(companyId, SubscriptionStatus.ACTIVE);

            if (activeOpt.isEmpty()) {
                log.info("No active subscription found for deleted company: {}", companyId);
                return;
            }

            Subscription subscription = activeOpt.get();

            // Cancel Stripe subscription if exists
            cancelStripeSubscription(subscription);

            // Set status to CANCELLED
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setEndDate(LocalDate.now());
            subscriptionRepository.save(subscription);

            log.info("Successfully cancelled subscription for deleted company. companyId={}, subscriptionId={}, stripeSubId={}",
                    companyId, subscription.getId(), subscription.getStripeSubscriptionId());

        } catch (Exception e) {
            log.error("Failed to process UserDeletedEvent: {}", e.getMessage(), e);
            // In production, consider dead letter queue for failed messages
            throw new RuntimeException("Failed to process UserDeletedEvent", e);
        }
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
            // This prevents orphaned billing but the Stripe subscription may need manual cleanup
            log.error("Failed to cancel Stripe subscription {}: {}", stripeSubId, e.getMessage());
        }
    }
}
