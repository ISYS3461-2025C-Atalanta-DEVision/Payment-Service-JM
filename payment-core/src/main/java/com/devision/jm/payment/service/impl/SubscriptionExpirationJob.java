package com.devision.jm.payment.service.impl;

import com.devision.jm.payment.api.internal.interfaces.ExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpirationJob {

  private final ExpirationService expirationService;

  // Run daily at 00:05 (Vietnam time)
  @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
  public void runDaily() {
    log.info("⏰ Running daily expiration check...");
    expirationService.runNow();
  }
}

