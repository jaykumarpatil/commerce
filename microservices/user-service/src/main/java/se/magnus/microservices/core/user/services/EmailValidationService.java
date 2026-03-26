package se.magnus.microservices.core.user.services;

import org.springframework.stereotype.Service;
import org.passay.*;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class EmailValidationService {

    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    private static final String[] DISPOSABLE_DOMAINS = {
        "mailinator.com", "guerrillamail.com", "tempmail.com", "10minutemail.com",
        "throwaway.email", "fakeinbox.com", "trashmail.com", "yopmail.com",
        "getnada.com", "mohmal.com", "temp-mail.org", "dispostable.com"
    };

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        
        if (!isValidLength(email)) {
            return false;
        }
        
        if (!hasValidStructure(email)) {
            return false;
        }
        
        return true;
    }

    public List<String> getValidationErrors(String email) {
        List<String> errors = new java.util.ArrayList<>();
        
        if (email == null || email.isBlank()) {
            errors.add("Email is required");
            return errors;
        }
        
        if (email.length() > 254) {
            errors.add("Email must not exceed 254 characters");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format (RFC5322)");
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            errors.add("Email must contain exactly one @ symbol");
        } else {
            if (parts[0].isEmpty()) {
                errors.add("Email local part cannot be empty");
            }
            if (parts[0].length() > 64) {
                errors.add("Email local part must not exceed 64 characters");
            }
            if (parts[1].length() > 255) {
                errors.add("Email domain must not exceed 255 characters");
            }
            if (!hasValidDomain(parts[1])) {
                errors.add("Email domain is invalid");
            }
        }
        
        if (isDisposableEmail(email)) {
            errors.add("Disposable email addresses are not allowed");
        }
        
        return errors;
    }

    public boolean isDisposableEmail(String email) {
        if (email == null) {
            return false;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (domain.equals(disposable)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidLength(String email) {
        return email.length() <= 254 && email.split("@")[0].length() <= 64;
    }

    private boolean hasValidStructure(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.isEmpty() || domain.isEmpty()) {
            return false;
        }
        
        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return false;
        }
        
        if (localPart.contains("..")) {
            return false;
        }
        
        if (domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        
        return true;
    }

    private boolean hasValidDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        
        if (domain.contains("..")) {
            return false;
        }
        
        if (domain.startsWith("-") || domain.endsWith("-")) {
            return false;
        }
        
        if (domain.startsWith(".") || domain.endsWith(".")) {
            return false;
        }
        
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        
        for (String label : labels) {
            if (label.isEmpty()) {
                return false;
            }
            if (label.length() > 63) {
                return false;
            }
        }
        
        return true;
    }

    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
