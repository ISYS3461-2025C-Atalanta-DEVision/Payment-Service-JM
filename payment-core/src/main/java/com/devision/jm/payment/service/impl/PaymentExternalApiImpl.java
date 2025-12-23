package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import com.devision.jm.payment.api.internal.dto.CreateSubscriptionCommand;
import com.devision.jm.payment.api.internal.dto.CreateSubscriptionResult;
import com.devision.jm.payment.api.internal.interfaces.SubscriptionBillingService;
import org.springframework.stereotype.Service;

@Service
public class PaymentExternalApiImpl implements PaymentExternalApi {

    private final SubscriptionBillingService subscriptionBillingService;
    private final StripeWebhookHandler stripeWebhookHandler;

    public PaymentExternalApiImpl(
            SubscriptionBillingService subscriptionBillingService,
            StripeWebhookHandler stripeWebhookHandler
    ) {
        this.subscriptionBillingService = subscriptionBillingService;
        this.stripeWebhookHandler = stripeWebhookHandler;
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
    public String handleStripeWebhook(String payload, String stripeSignature) {
        return stripeWebhookHandler.handleWebhook(payload, stripeSignature);
    }
}
