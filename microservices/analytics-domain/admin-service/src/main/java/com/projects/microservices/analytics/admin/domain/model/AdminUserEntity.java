package com.projects.microservices.analytics.admin.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "admin_users")
public class AdminUserEntity {
    @Id
    private String id;
    private String userId;
    private String username;
    private String role;
    private boolean active;
}
