package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.internal.dto.CreateSubscriptionCommand;
import com.devision.jm.payment.api.internal.dto.CreateSubscriptionResult;

public interface SubscriptionBillingService {

    CreateSubscriptionResult createSubscriptionIntent(CreateSubscriptionCommand command);
}
