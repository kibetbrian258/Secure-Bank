package com.application.secureBank.Services;

import org.springframework.stereotype.Service;

@Service
public class PhoneNumberService {

    private static final String KENYA_PREFIX = "+254";

    /**
     * Formats a phone number by adding the Kenyan prefix
     * @param phoneNumber The raw phone number (e.g., 712345678)
     * @return Formatted phone number with Kenya prefix (e.g., +254712345678)
     */
    public String formatKenyanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }

        // Remove any non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");

        // If number already has country code (starts with 254), add + sign
        if (digitsOnly.startsWith("254")) {
            return "+" + digitsOnly;
        }

        // If number starts with 0, remove it before adding prefix
        if (digitsOnly.startsWith("0")) {
            digitsOnly = digitsOnly.substring(1);
        }

        // Add Kenya prefix
        return KENYA_PREFIX + digitsOnly;
    }

    /**
     * Extracts the significant digits from a Kenyan phone number
     * @param phoneNumber The full phone number (e.g., +254712345678)
     * @return Significant digits without prefix (e.g., 712345678)
     */
    public String extractSignificantDigits(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }

        // Remove any non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");

        // If number has country code, remove it
        if (digitsOnly.startsWith("254")) {
            return digitsOnly.substring(3);
        }

        // If number starts with 0, remove it
        if (digitsOnly.startsWith("0")) {
            return digitsOnly.substring(1);
        }

        return digitsOnly;
    }
}