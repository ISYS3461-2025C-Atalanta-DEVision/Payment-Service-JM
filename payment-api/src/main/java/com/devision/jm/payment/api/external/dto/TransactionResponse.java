package com.devision.jm.payment.api.external.dto;

public class TransactionResponse {

    private String transactionId;
    private String status;
    private String amount;
    private String currency;
    private String payerEmail;
    private String stripePaymentId;
    private String createdAt;

    public TransactionResponse() {
    }

    

    public TransactionResponse(
            String transactionId,
            String status,
            String amount,
            String currency,
            String payerEmail,
            String stripePaymentId,
            String createdAt
    ) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.payerEmail = payerEmail;
        this.stripePaymentId = stripePaymentId;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getStripePaymentId() {
        return stripePaymentId;
    }

    public void setStripePaymentId(String stripePaymentId) {
        this.stripePaymentId = stripePaymentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
