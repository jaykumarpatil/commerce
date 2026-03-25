package se.magnus.microservices.core.admin.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.admin.Banner;
import se.magnus.microservices.core.admin.persistence.BannerEntity;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    Banner entityToApi(BannerEntity entity);
    BannerEntity apiToEntity(Banner banner);
}
