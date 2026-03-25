package se.magnus.microservices.core.user.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserEntity {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    
    @Indexed(unique = true)
    private String username;
    
    @Indexed(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    private String passwordHash;
    private String role; // CUSTOMER, ADMIN
    private boolean enabled;
    private String createdAt;
    private String updatedAt;
}
