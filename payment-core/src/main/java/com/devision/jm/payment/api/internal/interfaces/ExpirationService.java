package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.external.interfaces.ExpirationCheckResponse;


public interface ExpirationService {
    ExpirationCheckResponse runNow();

}
