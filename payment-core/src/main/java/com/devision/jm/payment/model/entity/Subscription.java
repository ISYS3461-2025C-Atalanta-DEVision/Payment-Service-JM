package com.devision.jm.payment.model.entity;

import com.devision.jm.payment.model.enums.SubscriptionStatus;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "subscriptions")
public class Subscription extends BaseEntity {

    private String applicantId;
    private String companyId;

    private String payerEmail;
    private String planType;
    private String currency;

    private LocalDate startDate;
    private LocalDate endDate;

    private SubscriptionStatus status;

    private LocalDate lastRenewedDate;

    private LocalDate endingSoonNotifiedOn; // when we sent T-7
    private LocalDate endedNotifiedOn;      // when we sent T-0

    // Mongo id is String, so store lastTransactionId as String (ObjectId string)
    private String lastTransactionId;

    private String stripeSubscriptionId;


    public String getApplicantId() {
        return applicantId;
    }

    public void setEndingSoonNotifiedOn(LocalDate endingSoonNotifiedOn) {
        this.endingSoonNotifiedOn = endingSoonNotifiedOn;
    }

    public void setEndedNotifiedOn(LocalDate endedNotifiedOn) {
        this.endedNotifiedOn = endedNotifiedOn;
    }

    public LocalDate getEndingSoonNotifiedOn() {
        return endingSoonNotifiedOn;
    }

    public LocalDate getEndedNotifiedOn() {
        return endedNotifiedOn;
    }

    public String getCompanyId() {
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

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public void setLastRenewedDate(LocalDate lastRenewedDate) {
        this.lastRenewedDate = lastRenewedDate;
    }


}
