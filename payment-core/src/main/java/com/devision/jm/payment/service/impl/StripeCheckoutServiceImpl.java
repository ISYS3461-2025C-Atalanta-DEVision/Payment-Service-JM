package com.devision.jm.payment.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.devision.jm.payment.api.internal.interfaces.CheckoutService;
import com.devision.jm.payment.api.internal.dto.CheckoutCommand;
import com.devision.jm.payment.api.internal.dto.CheckoutSessionResult;
import java.util.UUID;

@Service
public class StripeCheckoutServiceImpl implements CheckoutService {
@Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public CheckoutSessionResult checkout(CheckoutCommand command) {
        SubscriptionRequest req = new SubscriptionRequest();
        req.setCompanyId(command.getCompanyId());
        req.setApplicantId(command.getApplicantId());
        req.setPayerEmail(command.getPayerEmail());
        req.setPlanType(command.getPlanType());
        req.setCurrency(command.getCurrency());

        try {
            StripeResponse response = checkoutPayment(req);

            // Generate a transactionId for the result
            UUID transactionId = UUID.randomUUID();

            return new CheckoutSessionResult(response.getCheckoutUrl(), response.getSessionId(), transactionId);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe checkout failed", e);
        }
    }

    public StripeResponse checkoutPayment(SubscriptionRequest req) throws StripeException {
        Stripe.apiKey = secretKey;

        validateRequest(req);

        long unitAmount = amountForPlan(req.getPlanType(), req.getCurrency());

        SessionCreateParams params
                = SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .setCustomerEmail(req.getPayerEmail())
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(req.getCurrency().toLowerCase())
                                                        .setUnitAmount(unitAmount)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Subscription Plan: " + req.getPlanType())
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        // metadata helps webhook identify who/what later
                        .putMetadata("companyId", req.getCompanyId() == null ? "" : req.getCompanyId().toString())
                        .putMetadata("applicantId", req.getApplicantId() == null ? "" : req.getApplicantId().toString())
                        .putMetadata("planType", req.getPlanType())
                        .build();

        Session session = Session.create(params);

        return new StripeResponse(session.getUrl(), session.getId());
    }

    private void validateRequest(SubscriptionRequest req) {
        if (req.getCompanyId() == null && req.getApplicantId() == null) {
            throw new IllegalArgumentException("Either companyId or applicantId must be provided");
        }
        if (req.getCompanyId() != null && req.getApplicantId() != null) {
            throw new IllegalArgumentException("Only one of companyId or applicantId should be provided");
        }
        if (req.getPayerEmail() == null || req.getPayerEmail().isBlank()) {
            throw new IllegalArgumentException("payerEmail must be provided");
        }
        if (req.getPlanType() == null || req.getPlanType().isBlank()) {
            throw new IllegalArgumentException("planType must be provided");
        }
        if (req.getCurrency() == null || req.getCurrency().isBlank()) {
            throw new IllegalArgumentException("currency must be provided");
        }
    }

    private long amountForPlan(String planType, String currency) {
        String p = planType.trim().toUpperCase();
        String c = currency.trim().toLowerCase();

        boolean isVnd = c.equals("vnd");

        return switch (p) {
            case "BASIC" ->
                isVnd ? 99000L : 999L;
            case "PREMIUM" ->
                isVnd ? 199000L : 1999L;
            default ->
                throw new IllegalArgumentException("Unknown planType: " + planType);
        };
    }
}
