package com.devision.jm.payment.api.external.interfaces;

import com.devision.jm.payment.api.external.dto.PaymentSuccessRequest;
import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;

public interface PaymentExternalApi {
  StripeResponse checkout(SubscriptionRequest request);

  /**
   * Process payment success and publish Kafka event
   * @param request contains userId and planType
   * @return confirmation message
   */
  String processPaymentSuccess(PaymentSuccessRequest request);
}
