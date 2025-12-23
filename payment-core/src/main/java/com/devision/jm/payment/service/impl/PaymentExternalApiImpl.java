package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.external.interfaces.ExpirationCheckResponse;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import com.devision.jm.payment.api.external.interfaces.PremiumStatusResponse;
import com.devision.jm.payment.api.internal.dto.CreateSubscriptionCommand;
import com.devision.jm.payment.api.internal.dto.CreateSubscriptionResult;
import com.devision.jm.payment.api.internal.interfaces.SubscriptionBillingService;
import org.springframework.stereotype.Service;
import com.devision.jm.payment.api.internal.interfaces.PaymentQueryService;
import com.devision.jm.payment.api.internal.interfaces.ExpirationService;
import com.devision.jm.payment.api.internal.interfaces.StripeWebhookService;


import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentExternalApiImpl implements PaymentExternalApi {

    private final SubscriptionBillingService subscriptionBillingService;
    private final PaymentQueryService paymentQueryService;
    private final StripeWebhookService stripeWebhookService;
    private final ExpirationService expirationService;

    public PaymentExternalApiImpl(
            SubscriptionBillingService subscriptionBillingService,
            PaymentQueryService paymentQueryService,
            StripeWebhookService stripeWebhookService,
            ExpirationService expirationService
    ) {
        this.subscriptionBillingService = subscriptionBillingService;
        this.paymentQueryService = paymentQueryService;
        this.stripeWebhookService = stripeWebhookService;
        this.expirationService = expirationService;
    }

    @Override
    public SubscriptionIntentResponse createSubscriptionIntent(SubscriptionRequest request) {
        CreateSubscriptionCommand cmd = new CreateSubscriptionCommand(
                request.getCompanyId(),
                request.getApplicantId(),
                request.getPayerEmail(),
                request.getPlanType(),
                request.getCurrency()
        );

        CreateSubscriptionResult result = subscriptionBillingService.createSubscriptionIntent(cmd);

        return new SubscriptionIntentResponse(
                result.getClientSecret(),
                result.getStripeSubscriptionId(),
                result.getStripePaymentIntentId(),
                result.getTransactionId()
        );
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId) {
        return paymentQueryService.getTransactionById(transactionId);
    }

    @Override
    public List<TransactionResponse> findTransactions(String payerEmail, UUID companyId, UUID applicantId, String status) {
        if (paymentQueryService == null) {
            return Collections.emptyList();
        }
        return paymentQueryService.findTransactions(payerEmail, companyId, applicantId, status);
    }

    @Override
    public SubscriptionResponse getSubscriptionById(String subscriptionId) {
        return paymentQueryService.getSubscriptionById(subscriptionId);
    }

    @Override
    public SubscriptionResponse getSubscriptionByStripeId(String stripeSubscriptionId) {
        return paymentQueryService.getSubscriptionByStripeId(stripeSubscriptionId);
    }

    @Override
    public PremiumStatusResponse getCompanyPremiumStatus(UUID companyId) {
        return paymentQueryService.getCompanyPremiumStatus(companyId);
    }

    @Override
    public SubscriptionResponse cancelCompanySubscription(UUID companyId, boolean cancelAtPeriodEnd) {
        return paymentQueryService.cancelCompanySubscription(companyId, cancelAtPeriodEnd);
    }

    @Override
    public void handleStripeWebhook(String payload, String stripeSignature) {
        stripeWebhookService.handleWebhook(payload, stripeSignature);
    }

    @Override
    public ExpirationCheckResponse runExpirationCheckNow() {
        return expirationService.runNow();
    }
}
