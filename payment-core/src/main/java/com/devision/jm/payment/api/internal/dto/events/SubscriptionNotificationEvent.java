package com.devision.jm.payment.api.internal.dto.events;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionNotificationEvent {
    private String eventType; // ENDING_SOON or ENDED
    private String userId;    // companyId (or applicantId)
    private String planType;  // PREMIUM
    private LocalDate endDate;
    private Integer daysLeft; // 7 for ENDING_SOON, 0 for ENDED
    private LocalDateTime occurredAt;

}
