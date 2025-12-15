package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.internal.dto.PaymentCompletedEvent;

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
}
