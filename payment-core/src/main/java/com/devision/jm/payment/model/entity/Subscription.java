package com.devision.jm.payment.model.entity;

import com.devision.jm.payment.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {

    private UUID applicantId;
    private UUID companyId;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDate lastRenewedDate;

    // Optional (not a relation, just a stored UUID)
    private UUID lastTransactionId;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}
