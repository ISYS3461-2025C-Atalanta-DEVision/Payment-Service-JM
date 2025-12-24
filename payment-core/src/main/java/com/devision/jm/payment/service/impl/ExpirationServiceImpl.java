package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.ExpirationCheckResponse;
import com.devision.jm.payment.api.internal.interfaces.ExpirationService;
import com.devision.jm.payment.model.entity.Subscription;
import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public ExpirationServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public ExpirationCheckResponse runNow() {
        log.info("Running subscription expiration check...");

        LocalDate today = LocalDate.now();
        LocalDate soonThreshold = today.plusDays(7); // Expiring within 7 days

        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        int expiringSoonCount = 0;
        int expiredCount = 0;

        for (Subscription sub : allSubscriptions) {
            if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
                continue;
            }

            LocalDate endDate = sub.getEndDate();
            if (endDate == null) {
                continue;
            }

            // Check if already expired
            if (endDate.isBefore(today)) {
                log.info("Subscription {} has expired (endDate: {})", sub.getId(), endDate);
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
                expiredCount++;
            }
            // Check if expiring soon
            else if (endDate.isBefore(soonThreshold)) {
                log.info("Subscription {} is expiring soon (endDate: {})", sub.getId(), endDate);
                expiringSoonCount++;
            }
        }

        log.info("Expiration check complete: {} expiring soon, {} expired", expiringSoonCount, expiredCount);

        return new ExpirationCheckResponse(expiringSoonCount, expiredCount);
    }
}
