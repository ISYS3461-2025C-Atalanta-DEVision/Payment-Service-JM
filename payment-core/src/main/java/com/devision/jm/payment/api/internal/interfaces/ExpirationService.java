package com.devision.jm.payment.api.internal.interfaces;

import com.devision.jm.payment.api.external.dto.ExpirationCheckResponse;


public interface ExpirationService {
    ExpirationCheckResponse runNow();

}
