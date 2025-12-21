package com.devision.jm.payment.model.entity;

import com.devision.jm.payment.model.enums.TransactionStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction extends BaseEntity {

    // Reference to subscription document id
    private String subscriptionId;

    private Long amount;     // store cents
    private String currency;

    private String payerEmail;

    private String stripeCheckoutSessionId;
    private String stripePaymentId;

    private TransactionStatus status;
}
