/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * STIG V-220632 (NIST SI-10): CSRF token generation and validation.
 * Generates cryptographically secure tokens for state-changing operations
 * and validates them using constant-time comparison.
 */
public final class CSRFProtection {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CSRFProtection() { }

    /**
     * Generate a cryptographically secure CSRF token.
     */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Validate a CSRF token using constant-time comparison.
     */
    public static boolean validateToken(String token, String expected) {
        if (token == null || expected == null) {
            return false;
        }
        if (token.length() != expected.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < token.length(); i++) {
            result |= token.charAt(i) ^ expected.charAt(i);
        }
        return result == 0;
    }
}
