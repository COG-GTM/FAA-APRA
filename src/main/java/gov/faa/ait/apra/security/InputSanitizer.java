/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.security;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Input sanitization utility class implementing STIG V-220632 requirements.
 * Removes dangerous characters to prevent injection attacks.
 * 
 * @author FAA
 */
public final class InputSanitizer {
    
    private static final int DEFAULT_MAX_LENGTH = 255;
    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile("[<>\"';|&$()\\\\`]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\x00-\\x1F\\x7F]");
    
    private InputSanitizer() {
    }
    
    /**
     * Sanitizes a string by removing dangerous characters and trimming.
     * Implements STIG V-220632 requirements for input sanitization.
     * 
     * @param input the input string to sanitize
     * @return sanitized string, or null if input is null
     */
    public static String sanitizeString(String input) {
        return sanitizeString(input, DEFAULT_MAX_LENGTH);
    }
    
    /**
     * Sanitizes a string by removing dangerous characters, trimming, and enforcing max length.
     * 
     * @param input the input string to sanitize
     * @param maxLength maximum allowed length after sanitization
     * @return sanitized string, or null if input is null
     */
    public static String sanitizeString(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        
        String sanitized = input.trim();
        sanitized = CONTROL_CHARS_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = DANGEROUS_CHARS_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = WHITESPACE_PATTERN.matcher(sanitized).replaceAll(" ");
        
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized.trim();
    }
    
    /**
     * Sanitizes a geoname parameter.
     * 
     * @param geoname the geoname to sanitize
     * @return sanitized geoname
     */
    public static String sanitizeGeoname(String geoname) {
        if (geoname == null) {
            return null;
        }
        String sanitized = sanitizeString(geoname, 100);
        if (sanitized == null || sanitized.isEmpty()) {
            return null;
        }
        return sanitized;
    }
    
    /**
     * Sanitizes and normalizes an edition parameter.
     * 
     * @param edition the edition to sanitize
     * @return sanitized and lowercased edition, or null if invalid
     */
    public static String sanitizeEdition(String edition) {
        if (edition == null) {
            return null;
        }
        String sanitized = sanitizeString(edition, 20);
        if (sanitized == null || sanitized.isEmpty()) {
            return null;
        }
        return sanitized.toLowerCase(Locale.ENGLISH);
    }
    
    /**
     * Sanitizes and normalizes a format parameter.
     * 
     * @param format the format to sanitize
     * @return sanitized and lowercased format, or null if invalid
     */
    public static String sanitizeFormat(String format) {
        if (format == null) {
            return null;
        }
        String sanitized = sanitizeString(format, 10);
        if (sanitized == null || sanitized.isEmpty()) {
            return null;
        }
        return sanitized.toLowerCase(Locale.ENGLISH);
    }
    
    /**
     * Encodes special HTML characters to prevent XSS attacks.
     * 
     * @param input the input to encode
     * @return HTML-encoded string
     */
    public static String encodeHtml(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder encoded = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '<':
                    encoded.append("&lt;");
                    break;
                case '>':
                    encoded.append("&gt;");
                    break;
                case '&':
                    encoded.append("&amp;");
                    break;
                case '"':
                    encoded.append("&quot;");
                    break;
                case '\'':
                    encoded.append("&#x27;");
                    break;
                default:
                    encoded.append(c);
            }
        }
        return encoded.toString();
    }
    
    /**
     * Sanitizes input for use in log messages to prevent log injection.
     * 
     * @param input the input to sanitize for logging
     * @return log-safe string
     */
    public static String sanitizeForLog(String input) {
        if (input == null) {
            return "[null]";
        }
        String sanitized = input.replaceAll("[\r\n]", " ");
        sanitized = CONTROL_CHARS_PATTERN.matcher(sanitized).replaceAll("");
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500) + "...[truncated]";
        }
        return sanitized;
    }
    
    /**
     * Removes all non-alphanumeric characters except spaces, hyphens, and underscores.
     * 
     * @param input the input to clean
     * @return cleaned string
     */
    public static String cleanToAlphanumericExtended(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^a-zA-Z0-9\\s\\-_]", "").trim();
    }
}
