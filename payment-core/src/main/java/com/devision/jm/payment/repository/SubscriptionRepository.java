package com.devision.jm.payment.repository;
import com.devision.jm.payment.model.entity.Subscription;
import com.devision.jm.payment.model.enums.SubscriptionStatus;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findByCompanyIdOrderByCreatedAtDesc(String companyId);

    Optional<Subscription> findFirstByCompanyIdAndStatusOrderByEndDateDesc(String companyId, SubscriptionStatus status);

    List<Subscription> findByApplicantIdOrderByCreatedAtDesc(String applicantId);

    Optional<Subscription> findFirstByApplicantIdAndStatusOrderByEndDateDesc(String applicantId, SubscriptionStatus status);
}
