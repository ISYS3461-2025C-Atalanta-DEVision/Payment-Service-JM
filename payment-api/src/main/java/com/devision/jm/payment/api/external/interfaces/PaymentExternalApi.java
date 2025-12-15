package com.devision.jm.payment.api.external.interfaces;

import com.devision.jm.payment.api.external.dto.StripeResponse;
import com.devision.jm.payment.api.external.dto.SubscriptionRequest;

public interface PaymentExternalApi {
  StripeResponse checkout(SubscriptionRequest request);
  
}
