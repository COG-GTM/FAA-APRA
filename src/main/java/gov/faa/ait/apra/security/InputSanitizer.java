/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.util.regex.Pattern;

/**
 * STIG V-220632 (NIST SI-10): Input sanitization to prevent injection attacks.
 * Strips dangerous characters from user-supplied input.
 */
public final class InputSanitizer {

    private static final int DEFAULT_MAX_LENGTH = 255;
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("[<>\"';&#|`$()\\\\]");
    private static final Pattern NULL_BYTES = Pattern.compile("\\x00");

    private InputSanitizer() { }

    /**
     * Sanitize a user-supplied string by removing dangerous characters.
     * Returns null if the input is null, empty, exceeds max length, or is
     * empty after sanitization.
     */
    public static String sanitize(String input, int maxLength) {
        if (input == null || input.isEmpty() || input.length() > maxLength) {
            return null;
        }
        String sanitized = input.trim();
        sanitized = NULL_BYTES.matcher(sanitized).replaceAll("");
        sanitized = DANGEROUS_CHARS.matcher(sanitized).replaceAll("");
        return sanitized.isEmpty() ? null : sanitized;
    }

    /**
     * Sanitize with default max length of 255 characters.
     */
    public static String sanitize(String input) {
        return sanitize(input, DEFAULT_MAX_LENGTH);
    }

    /**
     * Sanitize a geoname parameter specifically.
     */
    public static String sanitizeGeoname(String geoname) {
        return sanitize(geoname, 100);
    }

    /**
     * Encode a string for safe inclusion in log output.
     * Prevents log injection by stripping newlines and control characters.
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        return input.replaceAll("[\\r\\n\\t]", "_")
                     .replaceAll("[\\x00-\\x1F\\x7F]", "");
    }
}
