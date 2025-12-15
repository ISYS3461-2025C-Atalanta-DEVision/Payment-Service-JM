package com.devision.jm.payment.service.impl;
import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;
import com.devision.jm.payment.api.external.interfaces.PaymentExternalApi;
import com.devision.jm.payment.exception.PaymentException;
import com.stripe.exception.StripeException;
import org.springframework.stereotype.Service;

@Service
public class PaymentExternalApiImpl implements PaymentExternalApi {

  private final StripeService stripeService;

  public PaymentExternalApiImpl(StripeService stripeService) {
    this.stripeService = stripeService;
  }

  @Override
  public StripeResponse checkout(SubscriptionRequest request) {
    try {
      return stripeService.checkoutPayment(request);
    } catch (StripeException e) {
      throw new PaymentException("Stripe checkout failed", e);
    }
  }
}
