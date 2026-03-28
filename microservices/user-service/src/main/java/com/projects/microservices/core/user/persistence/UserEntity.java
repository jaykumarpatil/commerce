package com.projects.microservices.core.user.persistence;

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
}
