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

    public UUID getApplicantId() {
        return applicantId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public String getPlanType() {
        return planType;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public LocalDate getLastRenewedDate() {
        return lastRenewedDate;
    }

    public String getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(String lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }

}
