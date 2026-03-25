package se.magnus.microservices.core.admin.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("admin_users")
public class AdminUserEntity {
    @Id
    private Long id;
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // ADMIN, STAFF
    private boolean enabled;
    private java.time.LocalDateTime lastLogin;
    private String createdAt;
}
