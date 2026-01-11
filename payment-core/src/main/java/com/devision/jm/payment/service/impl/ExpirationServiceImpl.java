package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.ExpirationCheckResponse;
import com.devision.jm.payment.api.internal.dto.events.SubscriptionNotificationEvent;
import com.devision.jm.payment.api.internal.dto.ja.PremiumJAExpiredEvent;
import com.devision.jm.payment.api.internal.interfaces.ExpirationService;
import com.devision.jm.payment.api.internal.interfaces.KafkaProducerService;
import com.devision.jm.payment.model.entity.Subscription;
import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Expiration Service Implementation
 *
 * Handles subscription expiration checks and notifications.
 */
@Slf4j
@Service
public class ExpirationServiceImpl implements ExpirationService {

    private final SubscriptionRepository subscriptionRepository;
    private final KafkaProducerService kafkaProducerService;

    public ExpirationServiceImpl(
        SubscriptionRepository subscriptionRepository,
        KafkaProducerService kafkaProducerService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public ExpirationCheckResponse runNow() {
        LocalDate today = LocalDate.now();
        LocalDate t7 = today.plusDays(7);

        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        int endingSoonNotified = 0;
        int endedNotified = 0;
        int expiredCount = 0;

        for (Subscription sub : allSubscriptions) {
            if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
                continue;
            }

            LocalDate endDate = sub.getEndDate();
            if (endDate == null) continue;

            String userId = sub.getCompanyId() != null ? sub.getCompanyId() : sub.getApplicantId();
            if (userId == null || userId.isBlank()) continue;

            // T-7 notify (exactly 7 days before end)
            if (endDate.isEqual(t7)) {
                if (sub.getEndingSoonNotifiedOn() == null || !sub.getEndingSoonNotifiedOn().isEqual(today)) {
                    kafkaProducerService.publishSubscriptionNotificationEvent(
                        SubscriptionNotificationEvent.builder()
                            .eventType("ENDING_SOON")
                            .userId(userId)
                            .planType(sub.getPlanType())
                            .endDate(endDate)
                            .daysLeft(7)
                            .occurredAt(LocalDateTime.now())
                            .build()
                    );
                    sub.setEndingSoonNotifiedOn(today);
                    subscriptionRepository.save(sub);
                    endingSoonNotified++;
                }

                log.info("🔔 EXPIRATION EVENT {} userId={} plan={} endDate={} today={}",
        "ENDING_SOON", userId, sub.getPlanType(), endDate, today);

            }

            // T-0 notify + expire on the end day
            if (endDate.isEqual(today)) {
                if (sub.getEndedNotifiedOn() == null || !sub.getEndedNotifiedOn().isEqual(today)) {
                    kafkaProducerService.publishSubscriptionNotificationEvent(
                        SubscriptionNotificationEvent.builder()
                            .eventType("ENDED")
                            .userId(userId)
                            .planType(sub.getPlanType())
                            .endDate(endDate)
                            .daysLeft(0)
                            .occurredAt(LocalDateTime.now())
                            .build()
                    );

                    // Publish JA team expired event if this is an applicant subscription
                    if (sub.getApplicantId() != null && !sub.getApplicantId().isBlank()) {
                        PremiumJAExpiredEvent jaEvent = PremiumJAExpiredEvent.builder()
                            .applicantId(sub.getApplicantId())
                            .subscriptionId(sub.getStripeSubscriptionId())
                            .expiredAt(LocalDateTime.now().toString())
                            .build();
                        kafkaProducerService.publishPremiumJAExpiredEvent(jaEvent);
                        log.info("📤 Published PremiumJAExpiredEvent for applicantId={}", sub.getApplicantId());
                    }

                    sub.setEndedNotifiedOn(today);
                    sub.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(sub);
                    endedNotified++;
                    expiredCount++;
                }
                log.info("🔔 EXPIRATION EVENT {} userId={} plan={} endDate={} today={}",
        "ENDED", userId, sub.getPlanType(), endDate, today);

            }

            // Safety: if somehow endDate already passed but still active
            if (endDate.isBefore(today)) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
                expiredCount++;
            }
        }

        // reuse your response fields if you want, or create new response fields
        return new ExpirationCheckResponse(endingSoonNotified, expiredCount);
    }
}
