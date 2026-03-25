package se.magnus.api.core.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private String role; // CUSTOMER, ADMIN
    private boolean enabled;
    private String createdAt;
    private String updatedAt;
}
