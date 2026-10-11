package lk.sliit.recruitment.service;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

    private final SystemSettingService settings;

    public PasswordPolicy(SystemSettingService settings) { this.settings = settings; }

    public void validate(String password) {
        int min = settings.getInt(SystemSettingService.PASSWORD_MIN_LENGTH, 8);
        if (password == null || password.length() < min) {
            throw new AdminOperationException("Password must be at least " + min + " characters");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            throw new AdminOperationException("Password must contain uppercase, lowercase and a number");
        }
    }
}
