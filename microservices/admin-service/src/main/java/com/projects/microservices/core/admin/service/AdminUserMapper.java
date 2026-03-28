package com.projects.microservices.core.admin.service;

import org.mapstruct.Mapper;
import com.projects.api.core.admin.AdminUser;
import com.projects.microservices.core.admin.persistence.AdminUserEntity;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    AdminUser entityToApi(AdminUserEntity entity);
    AdminUserEntity apiToEntity(AdminUser user);
}
