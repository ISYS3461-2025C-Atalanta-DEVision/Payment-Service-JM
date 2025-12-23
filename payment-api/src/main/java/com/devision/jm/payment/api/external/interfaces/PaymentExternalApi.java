package com.devision.jm.payment.api.external.interfaces;

import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.external.dto.PremiumStatusResponse;
import com.devision.jm.payment.api.external.dto.ExpirationCheckResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentExternalApi {

    SubscriptionIntentResponse createSubscriptionIntent(SubscriptionRequest request);

    TransactionResponse getTransactionById(String transactionId);

    List<TransactionResponse> findTransactions(String payerEmail, UUID companyId, UUID applicantId, String status);

    SubscriptionResponse getSubscriptionById(String subscriptionId);

    SubscriptionResponse getSubscriptionByStripeId(String stripeSubscriptionId);

    PremiumStatusResponse getCompanyPremiumStatus(UUID companyId);

    SubscriptionResponse cancelCompanySubscription(UUID companyId, boolean cancelAtPeriodEnd);

    void handleStripeWebhook(String payload, String stripeSignature);

    ExpirationCheckResponse runExpirationCheckNow();
}
