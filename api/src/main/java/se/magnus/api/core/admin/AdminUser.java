package se.magnus.api.core.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
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
