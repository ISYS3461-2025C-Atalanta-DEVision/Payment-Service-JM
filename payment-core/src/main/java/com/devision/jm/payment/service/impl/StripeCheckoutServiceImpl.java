package com.devision.jm.payment.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.devision.jm.payment.api.internal.interfaces.CheckoutService;
import com.devision.jm.payment.repository.TransactionRepository;
import com.devision.jm.payment.api.internal.dto.CheckoutCommand;
import com.devision.jm.payment.api.internal.dto.CheckoutSessionResult;
import com.devision.jm.payment.model.entity.Transaction;
import com.devision.jm.payment.model.enums.TransactionStatus;

@Service
public class StripeCheckoutServiceImpl implements CheckoutService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    private final TransactionRepository transactionRepository;

    public StripeCheckoutServiceImpl(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public CheckoutSessionResult checkout(CheckoutCommand command) {
        Stripe.apiKey = secretKey;
        validateCommand(command);
        long unitAmount = amountForPlan(command.getPlanType(), command.getCurrency());

        Transaction tx = new Transaction();
        tx.setSubscriptionId(null);
        tx.setAmount(unitAmount);
        tx.setCurrency(command.getCurrency());
        tx.setPayerEmail(command.getPayerEmail());
        tx.setStatus(TransactionStatus.PENDING);
        tx = transactionRepository.save(tx);

        try {
            SessionCreateParams params
                    = SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                            .setSuccessUrl(successUrl)
                            .setCancelUrl(cancelUrl)
                            .setCustomerEmail(command.getPayerEmail())
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency(command.getCurrency().toLowerCase())
                                                            .setUnitAmount(unitAmount)
                                                            .setRecurring(
                                                                    SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                            .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                            .build()
                                                            )
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName("PREMIUM Subscription")
                                                                            .build()
                                                            )
                                                            .build()
                                                                        
                                            )
                                            .build()
                            )
                            // metadata helps webhook identify who/what later
                            .putMetadata("transactionId", tx.getId())
                            .putMetadata("planType", command.getPlanType())
                            .putMetadata("companyId", command.getCompanyId() == null ? "" : command.getCompanyId().toString())
                            .putMetadata("applicantId", command.getApplicantId() == null ? "" : command.getApplicantId().toString())
                            .build();

            Session session = Session.create(params);

            tx.setStripeCheckoutSessionId(session.getId());
            tx = transactionRepository.save(tx);


            return new CheckoutSessionResult(session.getUrl(), session.getId(), tx.getId());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe API error: " + e.getMessage(), e);
        }
    }

    private void validateCommand(CheckoutCommand command) {
        if (command.getCompanyId() == null && command.getApplicantId() == null) {
            throw new IllegalArgumentException("Either companyId or applicantId must be provided");
        }
        if (command.getCompanyId() != null && command.getApplicantId() != null) {
            throw new IllegalArgumentException("Only one of companyId or applicantId should be provided");
        }
        if (command.getPayerEmail() == null || command.getPayerEmail().isBlank()) {
            throw new IllegalArgumentException("payerEmail must be provided");
        }
        if (command.getCurrency() == null || command.getCurrency().isBlank()) {
            throw new IllegalArgumentException("currency must be provided");
        }
        if (command.getPlanType() == null || command.getPlanType().isBlank()) {
            throw new IllegalArgumentException("planType must be provided");
        }
        if (!"PREMIUM".equalsIgnoreCase(command.getPlanType())) {
            throw new IllegalArgumentException("Checkout is only for PREMIUM");
        }
    }


    private long amountForPlan(String planType, String currency) {
        String c = currency.trim().toLowerCase();
        boolean isVnd = c.equals("vnd");
        return isVnd ? 300000L : 3000L;
    }

}
