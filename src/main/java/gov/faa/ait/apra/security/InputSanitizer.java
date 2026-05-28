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
 * STIG V-220632: Input sanitization to prevent injection attacks.
 * Strips dangerous characters that could be used for XSS, SQL injection,
 * or command injection.
 */
public final class InputSanitizer {

    private static final int DEFAULT_MAX_LENGTH = 255;
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("[<>\";&#|`$()\\\\]");
    private static final Pattern NULL_BYTES = Pattern.compile("\\x00");

    private InputSanitizer() { }

    /**
     * Sanitize a user-supplied string by removing dangerous characters.
     * Returns null if the input is null, empty, exceeds max length, or
     * becomes empty after sanitization.
     */
    public static String sanitize(String input) {
        return sanitize(input, DEFAULT_MAX_LENGTH);
    }

    /**
     * Sanitize a user-supplied string with a specified maximum length.
     */
    public static String sanitize(String input, int maxLength) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        if (input.length() > maxLength) {
            return null;
        }
        String sanitized = input.trim();
        sanitized = DANGEROUS_CHARS.matcher(sanitized).replaceAll("");
        sanitized = NULL_BYTES.matcher(sanitized).replaceAll("");
        return sanitized.isEmpty() ? null : sanitized;
    }

    /**
     * Encode output for safe inclusion in XML/HTML responses.
     */
    public static String encodeOutput(String input) {
        if (input == null) {
            return null;
        }
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}
