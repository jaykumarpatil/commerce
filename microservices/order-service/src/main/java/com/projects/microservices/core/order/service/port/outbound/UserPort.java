package com.projects.microservices.core.order.service.port.outbound;

import com.projects.api.core.user.User;
import reactor.core.publisher.Mono;

public interface UserPort {
    Mono<User> getUserById(String userId);
}
