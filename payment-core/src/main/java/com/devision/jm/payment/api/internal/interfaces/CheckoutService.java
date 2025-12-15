package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.internal.dto.CheckoutCommand;
import com.devision.jm.payment.api.internal.dto.CheckoutSessionResult;

public interface CheckoutService {

    CheckoutSessionResult checkout(CheckoutCommand command);
}
