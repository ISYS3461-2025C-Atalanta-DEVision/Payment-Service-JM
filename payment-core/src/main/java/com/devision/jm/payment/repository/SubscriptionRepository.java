package com.devision.jm.payment.repository;
import com.devision.jm.payment.model.entity.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

}
