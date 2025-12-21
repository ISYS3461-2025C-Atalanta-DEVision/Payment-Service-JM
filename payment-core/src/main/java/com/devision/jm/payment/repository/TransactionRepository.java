package com.devision.jm.payment.repository;
import com.devision.jm.payment.model.entity.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySubscriptionIdOrderByCreatedAtDesc(String subscriptionId);
    Optional<Transaction> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
}
