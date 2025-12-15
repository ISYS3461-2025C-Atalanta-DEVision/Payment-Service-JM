package com.devision.jm.payment.api.external.dto;

import java.util.UUID;

public class TransactionResponse {

    private UUID transactionId;
    private String status;
    private String amount;
    private String currency;
    private String payerEmail;
    private String stripePaymentId;
    private String createdAt;


    public TransactionResponse(UUID transactionId, String status, String amount, String currency, String payerEmail, String stripePaymentId, String createdAt) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.payerEmail = payerEmail;
        this.stripePaymentId = stripePaymentId;
        this.createdAt = createdAt;
    }
}
