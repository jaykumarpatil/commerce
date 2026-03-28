package com.projects.microservices.core.admin.service;

import org.mapstruct.Mapper;
import com.projects.api.core.admin.Coupon;
import com.projects.microservices.core.admin.persistence.CouponEntity;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    Coupon entityToApi(CouponEntity entity);
    CouponEntity apiToEntity(Coupon coupon);
}
