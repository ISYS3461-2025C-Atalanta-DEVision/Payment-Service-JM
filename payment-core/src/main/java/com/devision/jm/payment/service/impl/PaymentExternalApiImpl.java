package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import com.devision.jm.payment.api.internal.dto.CheckoutCommand;
import com.devision.jm.payment.api.internal.dto.CheckoutSessionResult;
import com.devision.jm.payment.api.internal.interfaces.CheckoutService;
import org.springframework.stereotype.Service;

@Service
public class PaymentExternalApiImpl implements PaymentExternalApi {

    private final CheckoutService checkoutService;

    public PaymentExternalApiImpl(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Override
    public StripeResponse checkout(SubscriptionRequest request) {
        CheckoutCommand cmd = new CheckoutCommand(
                request.getCompanyId(),
                request.getApplicantId(),
                request.getPayerEmail(),
                request.getPlanType(),
                request.getCurrency()
        );

        CheckoutSessionResult result = checkoutService.checkout(cmd);

        return new StripeResponse(result.getCheckoutUrl(), result.getSessionId());
    }
}
