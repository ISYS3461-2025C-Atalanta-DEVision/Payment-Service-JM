package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.internal.dto.CreateSubscriptionCommand;
import com.devision.jm.payment.api.internal.dto.CreateSubscriptionResult;
import com.devision.jm.payment.api.internal.interfaces.SubscriptionBillingService;
import com.devision.jm.payment.model.enums.SubscriptionStatus;
import com.devision.jm.payment.model.entity.Transaction;
import com.devision.jm.payment.model.enums.TransactionStatus;
import com.devision.jm.payment.repository.SubscriptionRepository;
import com.devision.jm.payment.repository.TransactionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;

@Service
public class StripeSubscriptionBillingServiceImpl implements SubscriptionBillingService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.price-id}")
    private String priceId;

    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public StripeSubscriptionBillingServiceImpl(
            TransactionRepository transactionRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public CreateSubscriptionResult createSubscriptionIntent(CreateSubscriptionCommand command) {
        Stripe.apiKey = secretKey;
        validateCommand(command);

        long unitAmount = amountForPlan(command.getPlanType(), "usd");

        // 1) tạo Transaction PENDING trước
        Transaction tx = new Transaction();
        tx.setSubscriptionId(null);
        tx.setAmount(unitAmount);
        tx.setCurrency("USD");
        tx.setPayerEmail(command.getPayerEmail());
        tx.setStatus(TransactionStatus.PENDING);
        tx = transactionRepository.save(tx);

        try {
            // 2) tạo Customer
            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(command.getPayerEmail())
                            .build()
            );

            // 3) chọn priceId theo currency
            String priceId = resolvePriceId(command.getCurrency());

            // 4) tạo Stripe Subscription (default_incomplete) + expand PI
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .setCustomer(customer.getId())
                    .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                    .addExpand("latest_invoice.payment_intent")
                    .addItem(
                            SubscriptionCreateParams.Item.builder()
                                    .setPrice(priceId)
                                    .build()
                    )
                    .putMetadata("transactionId", tx.getId())
                    .putMetadata("planType", command.getPlanType())
                    .putMetadata("companyId", command.getCompanyId() == null ? "" : command.getCompanyId())
                    .putMetadata("applicantId", command.getApplicantId() == null ? "" : command.getApplicantId())
                    .build();

            Subscription stripeSub = Subscription.create(params);

            try {
                SubscriptionUpdateParams updateParams = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                stripeSub = stripeSub.update(updateParams);
            } catch (StripeException e) {
                throw new RuntimeException("Failed to set cancel_at_period_end: " + e.getMessage(), e);
            }

            // 5) lấy PaymentIntent từ latest_invoice.payment_intent
            Invoice latestInvoice = stripeSub.getLatestInvoiceObject();
            if (latestInvoice == null && stripeSub.getLatestInvoice() != null) {
                latestInvoice = Invoice.retrieve(stripeSub.getLatestInvoice());
            }
            if (latestInvoice == null) {
                throw new RuntimeException("Stripe subscription has no latest_invoice");
            }

            PaymentIntent pi = latestInvoice.getPaymentIntentObject();
            if (pi == null && latestInvoice.getPaymentIntent() != null) {
                pi = PaymentIntent.retrieve(latestInvoice.getPaymentIntent());
            }
            if (pi == null) {
                throw new RuntimeException("Stripe invoice has no payment_intent");
            }

            String clientSecret = pi.getClientSecret();
            String stripeSubscriptionId = stripeSub.getId();
            String stripePaymentIntentId = pi.getId();

            // 6) update transaction mapping
            // NOTE: bạn đang dùng field "subscriptionId" để chứa stripeSubscriptionId (ok, miễn consistent)
            tx.setSubscriptionId(stripeSubscriptionId);
            tx.setStripePaymentId(stripePaymentIntentId);
            tx = transactionRepository.save(tx);

            // 7) tạo Subscription entity trong DB của mình để webhook tìm thấy mà update ACTIVE
            com.devision.jm.payment.model.entity.Subscription subEntity
                    = new com.devision.jm.payment.model.entity.Subscription();

            subEntity.setCompanyId(command.getCompanyId());
            subEntity.setApplicantId(command.getApplicantId());
            subEntity.setPayerEmail(command.getPayerEmail());
            subEntity.setPlanType(command.getPlanType());
            subEntity.setCurrency("USD");

            // lúc này chưa paid => coi như chưa active
            subEntity.setStatus(SubscriptionStatus.PENDING);

            subEntity.setStripeSubscriptionId(stripeSubscriptionId);
            subEntity.setLastTransactionId(tx.getId());

            long periodStartSec = stripeSub.getCurrentPeriodStart() == null ? 0L : stripeSub.getCurrentPeriodStart();
            long periodEndSec = stripeSub.getCurrentPeriodEnd() == null ? 0L : stripeSub.getCurrentPeriodEnd();

            ZoneId zone = ZoneId.of("UTC");

            LocalDate startDate = periodStartSec == 0L
                    ? LocalDate.now(zone)
                    : Instant.ofEpochSecond(periodStartSec).atZone(zone).toLocalDate();

            LocalDate endDate = periodEndSec == 0L
                    ? startDate.plusMonths(1)
                    : Instant.ofEpochSecond(periodEndSec).atZone(zone).toLocalDate();

            subEntity.setStartDate(startDate);
            subEntity.setEndDate(endDate); 

            subscriptionRepository.save(subEntity);

            return new CreateSubscriptionResult(
                    clientSecret,
                    stripeSubscriptionId,
                    stripePaymentIntentId,
                    tx.getId()
            );

        } catch (StripeException e) {
            throw new RuntimeException("Stripe API error: " + e.getMessage(), e);
        }
    }

    private String resolvePriceId(String currency) {
        if (priceId == null || priceId.isBlank()) {
            throw new IllegalArgumentException("Missing stripe.price-id in config");
        }
        return priceId;
    }

    private void validateCommand(CreateSubscriptionCommand command) {
    boolean hasCompanyId = command.getCompanyId() != null && !command.getCompanyId().isBlank();
    boolean hasApplicantId = command.getApplicantId() != null && !command.getApplicantId().isBlank();

        if (!hasCompanyId && !hasApplicantId) {
            throw new IllegalArgumentException("Either companyId or applicantId must be provided");
        }

        if (command.getPayerEmail() == null || command.getPayerEmail().isBlank()) {
            throw new IllegalArgumentException("payerEmail must be provided");
        }

        if (command.getPlanType() == null || command.getPlanType().isBlank()) {
            throw new IllegalArgumentException("planType must be provided");
        }

        if (!"PREMIUM".equalsIgnoreCase(command.getPlanType())) {
            throw new IllegalArgumentException("Only PREMIUM is supported");
        }
    }


    private long amountForPlan(String planType, String currency) {
        // $30 USD = 3000 cents
        return 3000L;
    }
}
