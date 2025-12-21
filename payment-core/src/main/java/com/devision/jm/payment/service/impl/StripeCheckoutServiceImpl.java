package com.devision.jm.payment.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        Stripe.apiKey = secretKey;

        long unitAmount = amountForPlan(command.getPlanType(), command.getCurrency());

        validateCommand(command);

        try{
            SessionCreateParams params
                    = SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
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
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName("Subscription Plan: " + command.getPlanType())
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            // metadata helps webhook identify who/what later
                            .putMetadata("companyId", command.getCompanyId() == null ? "" : command.getCompanyId().toString())
                            .putMetadata("applicantId", command.getApplicantId() == null ? "" : command.getApplicantId().toString())
                            .putMetadata("planType", command.getPlanType())
                            .build();

            Session session = Session.create(params);

            return new CheckoutSessionResult(
                    session.getUrl(),
                    session.getId(),
                    command.getCompanyId() != null ? command.getCompanyId() : command.getApplicantId());

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
        if (command.getPlanType() == null || command.getPlanType().isBlank()) {
            throw new IllegalArgumentException("planType must be provided");
        }
        if (command.getCurrency() == null || command.getCurrency().isBlank()) {
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
