package se.magnus.microservices.core.user.services;

import org.mapstruct.Mapper;
import se.magnus.api.core.user.User;
import se.magnus.microservices.core.user.persistence.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User entityToApi(UserEntity entity);
    UserEntity apiToEntity(User user);
}
