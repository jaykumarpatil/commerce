package se.magnus.microservices.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@CompoundIndex(name = "email_verification_idx", def = "{'email': 1, 'verificationToken': 1}")
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
    private String role;
    private boolean enabled;
    private boolean emailVerified;
    private String verificationToken;
    private Instant verificationTokenExpiry;
    private String passwordResetToken;
    private Instant passwordResetTokenExpiry;
    private Instant lastLoginAt;
    private String lastLoginIp;
    private int failedLoginAttempts;
    private Instant lockoutUntil;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private boolean deleted;
    
    public boolean isLocked() {
        return lockoutUntil != null && lockoutUntil.isAfter(Instant.now());
    }
    
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }
    
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockoutUntil = null;
    }
    
    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = Instant.now();
        this.email = "deleted_" + this.email;
        this.username = "deleted_" + this.username;
        this.enabled = false;
    }
    
    public void recordLogin(String ip) {
        this.lastLoginAt = Instant.now();
        this.lastLoginIp = ip;
        resetFailedAttempts();
    }
}
