package com.devision.jm.payment.api.external.interfaces;

import com.devision.jm.payment.api.external.dto.SubscriptionIntentResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.external.dto.PremiumStatusResponse;
import com.devision.jm.payment.api.external.dto.ExpirationCheckResponse;

import java.util.List;

public interface PaymentExternalApi {

    SubscriptionIntentResponse createSubscriptionIntent(SubscriptionRequest request);

    TransactionResponse getTransactionById(String transactionId);

    List<TransactionResponse> findTransactions(String payerEmail, String companyId, String applicantId, String status);

    SubscriptionResponse getSubscriptionById(String subscriptionId);

    SubscriptionResponse getSubscriptionByStripeId(String stripeSubscriptionId);

    PremiumStatusResponse getCompanyPremiumStatus(String companyId);

    SubscriptionResponse cancelCompanySubscription(String companyId, boolean cancelAtPeriodEnd);

    void handleStripeWebhook(String payload, String stripeSignature);

    ExpirationCheckResponse runExpirationCheckNow();

    List<SubscriptionResponse> getCompanySubscriptions(String companyId);

}
