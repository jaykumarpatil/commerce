package com.projects.microservices.core.payment.service;

import org.mapstruct.Mapper;
import com.projects.api.core.payment.Payment;
import com.projects.microservices.core.payment.persistence.PaymentEntity;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment entityToApi(PaymentEntity entity);
    PaymentEntity apiToEntity(Payment payment);
}
