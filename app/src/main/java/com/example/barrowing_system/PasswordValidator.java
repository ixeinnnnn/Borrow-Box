package com.example.barrowing_system;

import android.content.Context;
import android.widget.EditText;

/**
 * Password Validator Utility Class
 * Enforces strong password requirements:
 * - 12-16 characters
 * - At least 1 uppercase letter
 * - At least 1 lowercase letter
 * - At least 1 number
 * - At least 1 special character
 */
public class PasswordValidator {

    /**
     * Validates password strength and returns error message if invalid
     * @param password The password to validate
     * @param context Context for string resources
     * @return Error message if invalid, null if valid
     */
    public static String validatePassword(String password, Context context) {
        if (password == null || password.isEmpty()) {
            return context.getString(R.string.password_required);
        }

        if (password.length() < 12 || password.length() > 16) {
            return context.getString(R.string.password_min_length);
        }

        if (!password.matches(".*[A-Z].*")) {
            return context.getString(R.string.password_require_uppercase);
        }

        if (!password.matches(".*[a-z].*")) {
            return context.getString(R.string.password_require_lowercase);
        }

        if (!password.matches(".*[0-9].*")) {
            return context.getString(R.string.password_require_number);
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return context.getString(R.string.password_require_special);
        }

        return null;
    }

    /**
     * Validates password and sets error on EditText if invalid
     * @param password The password to validate
     * @param context Context for string resources
     * @param editText EditText to show error on
     * @return true if valid, false if invalid
     */
    public static boolean validatePasswordAndSetError(String password, Context context, EditText editText) {
        String errorMessage = validatePassword(password, context);
        
        if (errorMessage != null) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return false;
        }
        
        editText.setError(null);
        return true;
    }

    /**
     * Checks if password meets minimum requirements (for display purposes)
     * @param password The password to check
     * @return PasswordStrength object with validation results
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        PasswordStrength strength = new PasswordStrength();
        
        if (password == null) {
            return strength;
        }

        strength.hasValidLength = password.length() >= 12 && password.length() <= 16;
        strength.hasUppercase = password.matches(".*[A-Z].*");
        strength.hasLowercase = password.matches(".*[a-z].*");
        strength.hasNumber = password.matches(".*[0-9].*");
        strength.hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        strength.isStrong = strength.hasValidLength && strength.hasUppercase && 
                          strength.hasLowercase && strength.hasNumber && strength.hasSpecialChar;
        
        return strength;
    }

    /**
     * Gets password requirements text for display
     * @param context Context for string resources
     * @return Formatted requirements string
     */
    public static String getPasswordRequirements(Context context) {
        return "Password must contain:\n" +
               "• 12-16 characters\n" +
               "• At least 1 uppercase letter\n" +
               "• At least 1 lowercase letter\n" +
               "• At least 1 number\n" +
               "• At least 1 special character (!@#$%^&*())";
    }

    /**
     * Gets password strength text for display
     * @param password The password to check
     * @param context Context for string resources
     * @return Strength text (Weak, Medium, Strong)
     */
    public static String getPasswordStrengthText(String password, Context context) {
        PasswordStrength strength = checkPasswordStrength(password);
        
        if (strength.getStrengthPercentage() <= 40) {
            return context.getString(R.string.password_strength_weak);
        } else if (strength.getStrengthPercentage() <= 80) {
            return context.getString(R.string.password_strength_medium);
        } else {
            return context.getString(R.string.password_strength_strong);
        }
    }

    /**
     * Password strength validation results
     */
    public static class PasswordStrength {
        public boolean hasValidLength = false;
        public boolean hasUppercase = false;
        public boolean hasLowercase = false;
        public boolean hasNumber = false;
        public boolean hasSpecialChar = false;
        public boolean isStrong = false;
        
        public int getStrengthPercentage() {
            int requirementsMet = 0;
            if (hasValidLength) requirementsMet++;
            if (hasUppercase) requirementsMet++;
            if (hasLowercase) requirementsMet++;
            if (hasNumber) requirementsMet++;
            if (hasSpecialChar) requirementsMet++;
            
            return (requirementsMet * 100) / 5;
        }
    }
}
