package com.application.secureBank.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Custom validator for Kenyan phone numbers
 */
public class KenyanPhoneValidator implements ConstraintValidator<ValidKenyanPhone, String> {

    private static final Pattern KENYAN_PATTERN = Pattern.compile("^[7|1][0-9]{8}$");

    @Override
    public void initialize(ValidKenyanPhone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false; // Phone number is required
        }

        // Check if the number matches Kenyan pattern
        return KENYAN_PATTERN.matcher(phoneNumber).matches();
    }
}