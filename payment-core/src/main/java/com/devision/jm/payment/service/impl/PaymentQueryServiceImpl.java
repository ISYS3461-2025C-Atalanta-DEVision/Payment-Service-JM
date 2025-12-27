package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.PremiumStatusResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.internal.interfaces.PaymentQueryService;
import com.devision.jm.payment.model.entity.Subscription;
import com.devision.jm.payment.model.entity.Transaction;
import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import com.devision.jm.payment.repository.TransactionRepository;
import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Payment Query Service Implementation
 *
 * Handles read operations for transactions and subscriptions.
 */
@Slf4j
@Service
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${stripe.secret-key:}")
    private String stripeApiKey;

    public PaymentQueryServiceImpl(
            TransactionRepository transactionRepository,
            SubscriptionRepository subscriptionRepository) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId) {
        log.info("Getting transaction by id: {}", transactionId);
        Optional<Transaction> txOpt = transactionRepository.findById(transactionId);

        if (txOpt.isEmpty()) {
            log.warn("Transaction not found: {}", transactionId);
            return null;
        }

        Transaction tx = txOpt.get();
        return mapToTransactionResponse(tx);
    }

    @Override
    public List<TransactionResponse> findTransactions(String payerEmail, String companyId, String applicantId, String status) {
        log.info("Finding transactions - payerEmail: {}, companyId: {}, applicantId: {}, status: {}",
                payerEmail, companyId, applicantId, status);

        // For now, return all transactions - can be enhanced with filtering later
        List<Transaction> transactions = transactionRepository.findAll();

        return transactions.stream()
                .filter(tx -> payerEmail == null || payerEmail.equals(tx.getPayerEmail()))
                .filter(tx -> status == null || (tx.getStatus() != null && status.equals(tx.getStatus().name())))
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionResponse getSubscriptionById(String subscriptionId) {
        log.info("Getting subscription by id: {}", subscriptionId);
        Optional<Subscription> subOpt = subscriptionRepository.findById(subscriptionId);

        if (subOpt.isEmpty()) {
            log.warn("Subscription not found: {}", subscriptionId);
            return null;
        }

        return mapToSubscriptionResponse(subOpt.get());
    }

    @Override
    public SubscriptionResponse getSubscriptionByStripeId(String stripeSubscriptionId) {
        log.info("Getting subscription by Stripe id: {}", stripeSubscriptionId);
        Optional<Subscription> subOpt = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);

        if (subOpt.isEmpty()) {
            log.warn("Subscription not found for Stripe id: {}", stripeSubscriptionId);
            return null;
        }

        return mapToSubscriptionResponse(subOpt.get());
    }

    @Override
    public PremiumStatusResponse getCompanyPremiumStatus(String companyId) {
        log.info("Getting premium status for company: {}", companyId);

        Optional<Subscription> activeOpt
                = subscriptionRepository.findFirstByCompanyIdAndStatusOrderByEndDateDesc(
                        companyId, SubscriptionStatus.ACTIVE
                );

        if (activeOpt.isEmpty()) {
            return new PremiumStatusResponse(companyId, false, "NONE", null);
        }

        Subscription activeSub = activeOpt.get();

        LocalDate today = LocalDate.now();
        boolean isPremium
                = activeSub.getStatus() == SubscriptionStatus.ACTIVE
                && "PREMIUM".equalsIgnoreCase(activeSub.getPlanType())
                && (activeSub.getEndDate() == null || !activeSub.getEndDate().isBefore(today));

        return new PremiumStatusResponse(
                companyId,
                isPremium,
                activeSub.getStatus().name(),
                activeSub.getEndDate() != null ? activeSub.getEndDate().toString() : null
        );
    }


    @Override
    public SubscriptionResponse cancelCompanySubscription(String companyId, boolean cancelAtPeriodEnd) {
        log.info("Cancelling subscription for company: {}, cancelAtPeriodEnd: {}", companyId, cancelAtPeriodEnd);

        Subscription sub = subscriptionRepository
                .findFirstByCompanyIdAndStatusOrderByEndDateDesc(companyId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active subscription found for company: " + companyId));

        if (sub.getStripeSubscriptionId() == null || sub.getStripeSubscriptionId().isBlank()) {
            throw new RuntimeException("Subscription missing stripeSubscriptionId: " + sub.getId());
        }

        try {
            Stripe.apiKey = stripeApiKey;

            com.stripe.model.Subscription stripeSub
                    = com.stripe.model.Subscription.retrieve(sub.getStripeSubscriptionId());

            if (cancelAtPeriodEnd) {
                com.stripe.param.SubscriptionUpdateParams params
                        = com.stripe.param.SubscriptionUpdateParams.builder()
                                .setCancelAtPeriodEnd(true)
                                .build();
                stripeSub.update(params);

                sub.setStatus(SubscriptionStatus.CANCELLING);
            } else {
                stripeSub.cancel();
                sub.setStatus(SubscriptionStatus.CANCELLED);
            }

            subscriptionRepository.save(sub);
            return mapToSubscriptionResponse(sub);

        } catch (Exception e) {
            log.error("Stripe cancel failed for subId={}, stripeSubId={}",
                    sub.getId(), sub.getStripeSubscriptionId(), e);
            throw new RuntimeException("Stripe cancel failed: " + e.getMessage(), e);
        }
    }


    private TransactionResponse mapToTransactionResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getStatus() != null ? tx.getStatus().name() : null,
                tx.getAmount() != null ? String.valueOf(tx.getAmount()) : null,
                tx.getCurrency(),
                tx.getPayerEmail(),
                tx.getStripePaymentId(),
                tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null
        );
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription sub) {
        return new SubscriptionResponse(
                sub.getId(),
                sub.getStatus() != null ? sub.getStatus().name() : null,
                sub.getStartDate() != null ? sub.getStartDate().toString() : null,
                sub.getEndDate() != null ? sub.getEndDate().toString() : null
        );
    }

    @Override
    public List<SubscriptionResponse> getCompanySubscriptions(String companyId) {
        return subscriptionRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::mapToSubscriptionResponse)
                .collect(Collectors.toList());
    }

}
