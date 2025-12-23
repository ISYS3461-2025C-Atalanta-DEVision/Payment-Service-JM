package com.devision.jm.payment.mapper;

import com.devision.jm.payment.api.external.dto.TransactionResponse;
import com.devision.jm.payment.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "status", expression = "java(entity.getStatus() == null ? null : entity.getStatus().name())")
    @Mapping(target = "amount", expression = "java(entity.getAmount() == null ? null : String.valueOf(entity.getAmount()))")
    @Mapping(target = "stripePaymentId", source = "stripePaymentId")
    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString())")
    TransactionResponse toResponse(Transaction entity);
}
