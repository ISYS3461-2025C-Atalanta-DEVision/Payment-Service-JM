package com.devision.jm.payment.model.entity;

import com.devision.jm.payment.model.enums.TransactionStatus;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "transactions")
public class Transaction extends BaseEntity {

    // Reference to subscription document id
    private String subscriptionId;

    private String companyId;
    private String applicantId;

    private Long amount;     // store cents
    private String currency;

    private String payerEmail;

    private String stripeCheckoutSessionId;
    private String stripePaymentId;

    private TransactionStatus status;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public String getStripeCheckoutSessionId() {
        return stripeCheckoutSessionId;
    }

    public String getStripePaymentId() {
        return stripePaymentId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStripeCheckoutSessionId(String stripeCheckoutSessionId) {
        this.stripeCheckoutSessionId = stripeCheckoutSessionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public void setStripePaymentId(String stripePaymentId) {
        this.stripePaymentId = stripePaymentId;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

}
