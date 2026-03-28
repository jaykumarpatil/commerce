package com.projects.microservices.core.user.services;

import org.mapstruct.Mapper;
import com.projects.api.core.user.User;
import com.projects.microservices.core.user.persistence.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User entityToApi(UserEntity entity);
    UserEntity apiToEntity(User user);
}
