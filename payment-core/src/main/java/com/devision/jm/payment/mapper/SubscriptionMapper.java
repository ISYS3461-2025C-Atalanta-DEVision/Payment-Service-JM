package com.devision.jm.payment.mapper;

import com.devision.jm.payment.api.external.dto.SubscriptionResponse;
import com.devision.jm.payment.model.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping(target = "subscriptionId", source = "id")
    @Mapping(target = "status", expression = "java(entity.getStatus() == null ? null : entity.getStatus().name())")
    @Mapping(target = "startDate", expression = "java(entity.getStartDate() == null ? null : entity.getStartDate().toString())")
    @Mapping(target = "endDate", expression = "java(entity.getEndDate() == null ? null : entity.getEndDate().toString())")
    SubscriptionResponse toResponse(Subscription entity);
}
