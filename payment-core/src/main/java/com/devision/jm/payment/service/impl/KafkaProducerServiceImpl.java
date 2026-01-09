package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;
import com.devision.jm.payment.api.internal.dto.SubscriptionCancelledEvent;
import com.devision.jm.payment.api.internal.dto.events.SubscriptionNotificationEvent;
import com.devision.jm.payment.api.internal.interfaces.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer Service Implementation
 *
 * Publishes payment events to Kafka for consumption by other microservices.
 * Profile Service consumes these events to upgrade user subscriptions.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final String PAYMENT_COMPLETED_TOPIC = "payment-completed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerServiceImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    //payment completed event
    @Override
    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            log.info("========== PUBLISHING PAYMENT COMPLETED EVENT ==========");
            log.info("Topic: {}", PAYMENT_COMPLETED_TOPIC);
            log.info("UserId: {}", event.getUserId());
            log.info("PlanType: {}", event.getPlanType());
            log.info("PaidAt: {}", event.getPaidAt());
            log.info("=========================================================");

            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, event.getUserId(), eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Payment completed event published successfully for userId: {}",
                                    event.getUserId());
                        } else {
                            log.error("Failed to publish payment completed event for userId: {}. Error: {}",
                                    event.getUserId(), ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("Error serializing payment completed event for userId: {}. Error: {}",
                    event.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to publish payment completed event", e);
        }
    }

    //subscription notification event
    private static final String SUBSCRIPTION_NOTIFICATIONS_TOPIC = "subscription-notifications";

    @Override
    public void publishSubscriptionNotificationEvent(SubscriptionNotificationEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            log.info("========== PUBLISHING SUBSCRIPTION NOTIFICATION EVENT ==========");
            log.info("Topic: {}", SUBSCRIPTION_NOTIFICATIONS_TOPIC);
            log.info("EventType: {}", event.getEventType());
            log.info("UserId: {}", event.getUserId());
            log.info("PlanType: {}", event.getPlanType());
            log.info("EndDate: {}", event.getEndDate());
            log.info("DaysLeft: {}", event.getDaysLeft());
            log.info("================================================================");

            kafkaTemplate.send(SUBSCRIPTION_NOTIFICATIONS_TOPIC, event.getUserId(), eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Subscription notification event published successfully for userId: {}, eventType: {}",
                                    event.getUserId(), event.getEventType());
                        } else {
                            log.error("Failed to publish subscription notification event for userId: {}, eventType: {}. Error: {}",
                                    event.getUserId(), event.getEventType(), ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("Error serializing subscription notification event for userId: {}, eventType: {}. Error: {}",
                    event.getUserId(), event.getEventType(), e.getMessage());
            throw new RuntimeException("Failed to publish subscription notification event", e);
        }
    }

    //subscription cancelled event
    private static final String SUBSCRIPTION_CANCELLED_TOPIC = "subscription-cancelled";

    @Override
    public void publishSubscriptionCancelledEvent(SubscriptionCancelledEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            log.info("========== PUBLISHING SUBSCRIPTION CANCELLED EVENT ==========");
            log.info("Topic: {}", SUBSCRIPTION_CANCELLED_TOPIC);
            log.info("UserId: {}", event.getUserId());
            log.info("CompanyId: {}", event.getCompanyId());
            log.info("PreviousPlanType: {}", event.getPreviousPlanType());
            log.info("CancelledAt: {}", event.getCancelledAt());
            log.info("==============================================================");

            String key = event.getUserId() != null ? event.getUserId() : event.getCompanyId();
            kafkaTemplate.send(SUBSCRIPTION_CANCELLED_TOPIC, key, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Subscription cancelled event published successfully for userId: {}",
                                    event.getUserId());
                        } else {
                            log.error("Failed to publish subscription cancelled event for userId: {}. Error: {}",
                                    event.getUserId(), ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("Error serializing subscription cancelled event for userId: {}. Error: {}",
                    event.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to publish subscription cancelled event", e);
        }
    }
}
