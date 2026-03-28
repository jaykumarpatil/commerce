package com.projects.microservices.core.admin.service;

import org.mapstruct.Mapper;
import com.projects.api.core.admin.Banner;
import com.projects.microservices.core.admin.persistence.BannerEntity;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    Banner entityToApi(BannerEntity entity);
    BannerEntity apiToEntity(Banner banner);
}
