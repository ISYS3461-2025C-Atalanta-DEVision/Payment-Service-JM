package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.external.dto.PremiumStatusResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentQueryService {
    TransactionResponse getTransactionById(String transactionId);

    List<TransactionResponse> findTransactions(String payerEmail, UUID companyId, UUID applicantId, String status);

    SubscriptionResponse getSubscriptionById(String subscriptionId);

    SubscriptionResponse getSubscriptionByStripeId(String stripeSubscriptionId);

    PremiumStatusResponse getCompanyPremiumStatus(UUID companyId);

    SubscriptionResponse cancelCompanySubscription(UUID companyId, boolean cancelAtPeriodEnd);

}
