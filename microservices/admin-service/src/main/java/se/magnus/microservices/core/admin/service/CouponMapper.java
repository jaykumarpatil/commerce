package se.magnus.microservices.core.admin.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.admin.Coupon;
import se.magnus.microservices.core.admin.persistence.CouponEntity;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    Coupon entityToApi(CouponEntity entity);
    CouponEntity apiToEntity(Coupon coupon);
}
