package se.magnus.microservices.core.admin.service;

import org.mapstruct.Mapper;
import se.magnus.api.core.admin.AdminUser;
import se.magnus.microservices.core.admin.persistence.AdminUserEntity;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    AdminUser entityToApi(AdminUserEntity entity);
    AdminUserEntity apiToEntity(AdminUser user);
}
