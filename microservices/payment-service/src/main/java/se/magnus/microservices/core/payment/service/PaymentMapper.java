package se.magnus.microservices.core.payment.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.payment.Payment;
import se.magnus.microservices.core.payment.persistence.PaymentEntity;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment entityToApi(PaymentEntity entity);
    PaymentEntity apiToEntity(Payment payment);
}
