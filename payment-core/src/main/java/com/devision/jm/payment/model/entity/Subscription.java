package com.devision.jm.payment.model.entity;

import com.devision.jm.payment.model.enums.SubscriptionStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscriptions")
public class Subscription extends BaseEntity {


    private UUID applicantId;
    private UUID companyId;

    private String payerEmail;
    private String planType;
    private String currency;

    private LocalDate startDate;
    private LocalDate endDate;

    private SubscriptionStatus status;

    private LocalDate lastRenewedDate;

    // Mongo id is String, so store lastTransactionId as String (ObjectId string)
    private String lastTransactionId;
}
