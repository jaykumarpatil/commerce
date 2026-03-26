package se.magnus.microservices.core.user.services;

import org.passay.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordValidationService {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 128;

    public List<String> validatePassword(String password) {
        List<Rule> rules = new ArrayList<>();
        
        rules.add(new LengthRule(MIN_LENGTH, MAX_LENGTH));
        
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        
        rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        
        rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        
        rules.add(new WhitespaceRule());
        
        PasswordValidator validator = new PasswordValidator(rules);
        RuleResult result = validator.validate(new PasswordData(password));
        
        List<String> errors = new ArrayList<>();
        if (!result.isValid()) {
            for (RuleResultDetail detail : result.getDetails()) {
                errors.add(mapErrorCode(detail.getErrorCode()));
            }
        }
        
        return errors;
    }

    public boolean isPasswordStrong(String password) {
        return validatePassword(password).isEmpty();
    }

    public String getPasswordStrengthFeedback(String password) {
        List<String> errors = validatePassword(password);
        if (errors.isEmpty()) {
            return "Password is strong";
        }
        return String.join(". ", errors);
    }

    private String mapErrorCode(String errorCode) {
        return switch (errorCode) {
            case "TOO_SHORT" -> "Password must be at least " + MIN_LENGTH + " characters";
            case "TOO_LONG" -> "Password must not exceed " + MAX_LENGTH + " characters";
            case "INSUFFICIENT_UPPERCASE" -> "Password must contain at least one uppercase letter";
            case "INSUFFICIENT_LOWERCASE" -> "Password must contain at least one lowercase letter";
            case "INSUFFICIENT_DIGIT" -> "Password must contain at least one number";
            case "INSUFFICIENT_SPECIAL" -> "Password must contain at least one special character (!@#$%^&*)";
            case "CONTAINS_WHITESPACE" -> "Password must not contain whitespace";
            case "ILLEGAL_SEQUENCE" -> "Password must not contain repeated character sequences";
            default -> "Invalid password: " + errorCode;
        };
    }

    public boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        String[] commonPasswords = {
            "password", "password123", "123456", "12345678", "qwerty",
            "abc123", "monkey", "1234567", "letmein", "trustno1",
            "dragon", "baseball", "iloveyou", "master", "sunshine",
            "ashley", "bailey", "shadow", "123123", "654321",
            "superman", "qazwsx", "michael", "football", "password1",
            "password123!", "welcome", "welcome1", "admin", "login"
        };
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }
}
