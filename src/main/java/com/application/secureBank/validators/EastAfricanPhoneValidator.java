package com.application.secureBank.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom validator for East African phone numbers
 */
public class EastAfricanPhoneValidator implements ConstraintValidator<ValidEastAfricanPhone, String> {

    private static final Map<String, String> COUNTRY_PATTERNS = new HashMap<>();
    private static final Pattern GENERAL_PATTERN = Pattern.compile("^\\+(254|255|256|250|257|211|251|252|253|291)[0-9]{7,10}$");

    static {

        COUNTRY_PATTERNS.put("254", "^\\+254[7|1][0-9]{8}$"); // Kenya
        COUNTRY_PATTERNS.put("255", "^\\+255[6|7][0-9]{8}$"); // Tanzania
        COUNTRY_PATTERNS.put("256", "^\\+256[7|4][0-9]{8}$"); // Uganda
        COUNTRY_PATTERNS.put("250", "^\\+250[7][0-9]{8}$"); // Rwanda
        COUNTRY_PATTERNS.put("257", "^\\+257[7|2][0-9]{7}$"); // Burundi
        COUNTRY_PATTERNS.put("211", "^\\+211[9][0-9]{8}$"); // South Sudan
        COUNTRY_PATTERNS.put("251", "^\\+251[9][0-9]{8}$"); // Ethiopia
        COUNTRY_PATTERNS.put("252", "^\\+252[6][0-9]{8}$"); // Somalia
        COUNTRY_PATTERNS.put("253", "^\\+253[7][0-9]{7}$"); // Djibouti
        COUNTRY_PATTERNS.put("291", "^\\+291[7][0-9]{7}$"); // Eritrea
    }

    @Override
    public void initialize(ValidEastAfricanPhone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false; // Phone number is required
        }

        // First check if it matches the general East African pattern
        if (!GENERAL_PATTERN.matcher(phoneNumber).matches()) {
            return false;
        }

        // Extract the country code (the digits after the plus sign)
        String countryCode = phoneNumber.substring(1, 4); // +254 -> 254

        // Check against specific country pattern if available
        if (COUNTRY_PATTERNS.containsKey(countryCode)) {
            return Pattern.matches(COUNTRY_PATTERNS.get(countryCode), phoneNumber);
        }

        // If no specific pattern found, return true if it passed the general pattern
        return true;
    }
};