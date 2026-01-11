package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;
import com.devision.jm.payment.api.internal.dto.SubscriptionCancelledEvent;
import com.devision.jm.payment.api.internal.dto.events.SubscriptionNotificationEvent;
import com.devision.jm.payment.api.internal.dto.ja.PremiumJACreatedEvent;
import com.devision.jm.payment.api.internal.dto.ja.PremiumJAExpiredEvent;
import com.devision.jm.payment.api.internal.dto.ja.PremiumJAClosedEvent;

/**
 * Kafka Producer Service Interface
 *
 * Publishes events to Kafka for consumption by other microservices.
 */
public interface KafkaProducerService {

    /**
     * Publish payment completed event to Kafka
     * Topic: payment-completed
     * Consumer: Profile Service (upgrades user to PREMIUM)
     *
     * @param event the payment completed event
     */
    void publishPaymentCompletedEvent(PaymentCompletedEvent event);

    /**
     * Publish subscription notification event to Kafka
     * Topic: subscription-notifications
     * Consumer: Auth Service (sends emails), Profile Service (tracks notifications)
     *
     * @param event the subscription notification event
     */
    void publishSubscriptionNotificationEvent(SubscriptionNotificationEvent event);

    /**
     * Publish subscription cancelled event to Kafka
     * Topic: subscription-cancelled
     * Consumer: Profile Service (downgrades user to FREE)
     *
     * @param event the subscription cancelled event
     */
    void publishSubscriptionCancelledEvent(SubscriptionCancelledEvent event);

    // ==================== JA Team Events ====================

    /**
     * Publish premium JA created event to Kafka
     * Topic: subscription.premium.ja.created
     * Consumer: JA team services (sets applicant.isPremium = true)
     *
     * @param event the premium JA created event
     */
    void publishPremiumJACreatedEvent(PremiumJACreatedEvent event);

    /**
     * Publish premium JA expired event to Kafka
     * Topic: subscription.premium.ja.expired
     * Consumer: JA team services (sets applicant.isPremium = false)
     *
     * @param event the premium JA expired event
     */
    void publishPremiumJAExpiredEvent(PremiumJAExpiredEvent event);

    /**
     * Publish premium JA closed event to Kafka
     * Topic: subscription.premium.ja.closed
     * Consumer: JA team services (sets applicant.isPremium = false)
     *
     * @param event the premium JA closed event
     */
    void publishPremiumJAClosedEvent(PremiumJAClosedEvent event);
}
