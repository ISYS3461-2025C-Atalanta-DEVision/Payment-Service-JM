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
  @Mapping(target = "stripePaymentId", source = "stripePaymentId")
  TransactionResponse toResponse(Transaction entity);
}
