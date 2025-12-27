package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.api.external.dto.PremiumStatusResponse;

import java.util.List;

public interface PaymentQueryService {
    TransactionResponse getTransactionById(String transactionId);

    List<TransactionResponse> findTransactions(String payerEmail, String companyId, String applicantId, String status);

    SubscriptionResponse getSubscriptionById(String subscriptionId);

    SubscriptionResponse getSubscriptionByStripeId(String stripeSubscriptionId);

    PremiumStatusResponse getCompanyPremiumStatus(String companyId);

    SubscriptionResponse cancelCompanySubscription(String companyId, boolean cancelAtPeriodEnd);

    List<SubscriptionResponse> getCompanySubscriptions(String companyId);


}
