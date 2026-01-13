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

import java.util.regex.Pattern;

/**
 * Input validation utility class implementing STIG V-220631 requirements.
 * Uses whitelist-based validation to ensure only expected input patterns are accepted.
 * 
 * @author FAA
 */
public final class InputValidator {
    
    private static final int MAX_INPUT_LENGTH = 255;
    private static final int MAX_GEONAME_LENGTH = 100;
    private static final int MAX_EDITION_LENGTH = 20;
    private static final int MAX_FORMAT_LENGTH = 10;
    
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern GEONAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_]+$");
    private static final Pattern EDITION_PATTERN = Pattern.compile("^(current|next|changeset)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^(pdf|tiff|zip)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,]+$");
    
    private InputValidator() {
    }
    
    /**
     * Validates that input is not null, not empty, and within length limits.
     * 
     * @param input the input string to validate
     * @param maxLength maximum allowed length
     * @return true if input passes basic validation
     */
    public static boolean validateBasicInput(String input, int maxLength) {
        if (input == null) {
            return false;
        }
        String trimmed = input.trim();
        return !trimmed.isEmpty() && trimmed.length() <= maxLength;
    }
    
    /**
     * Validates geoname parameter using whitelist approach.
     * Geonames should only contain alphanumeric characters, spaces, hyphens, and underscores.
     * 
     * @param geoname the geoname to validate
     * @return true if geoname is valid
     */
    public static boolean validateGeoname(String geoname) {
        if (!validateBasicInput(geoname, MAX_GEONAME_LENGTH)) {
            return false;
        }
        return GEONAME_PATTERN.matcher(geoname.trim()).matches();
    }
    
    /**
     * Validates edition parameter against allowed values.
     * 
     * @param edition the edition to validate (current, next, or changeset)
     * @return true if edition is valid
     */
    public static boolean validateEdition(String edition) {
        if (!validateBasicInput(edition, MAX_EDITION_LENGTH)) {
            return false;
        }
        return EDITION_PATTERN.matcher(edition.trim()).matches();
    }
    
    /**
     * Validates format parameter against allowed values.
     * 
     * @param format the format to validate (pdf, tiff, or zip)
     * @return true if format is valid
     */
    public static boolean validateFormat(String format) {
        if (!validateBasicInput(format, MAX_FORMAT_LENGTH)) {
            return false;
        }
        return FORMAT_PATTERN.matcher(format.trim()).matches();
    }
    
    /**
     * Validates alphanumeric input only.
     * 
     * @param input the input to validate
     * @return true if input contains only alphanumeric characters
     */
    public static boolean validateAlphanumeric(String input) {
        if (!validateBasicInput(input, MAX_INPUT_LENGTH)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(input.trim()).matches();
    }
    
    /**
     * Validates a safe string that may contain common punctuation.
     * 
     * @param input the input to validate
     * @return true if input is a safe string
     */
    public static boolean validateSafeString(String input) {
        if (!validateBasicInput(input, MAX_INPUT_LENGTH)) {
            return false;
        }
        return SAFE_STRING_PATTERN.matcher(input.trim()).matches();
    }
    
    /**
     * Validates numeric range for integer values.
     * 
     * @param value the value to validate
     * @param minVal minimum allowed value
     * @param maxVal maximum allowed value
     * @return true if value is within range
     */
    public static boolean validateNumericRange(int value, int minVal, int maxVal) {
        return value >= minVal && value <= maxVal;
    }
    
    /**
     * Validates that a string represents a valid positive integer.
     * 
     * @param input the input string
     * @return true if input is a valid positive integer
     */
    public static boolean validatePositiveInteger(String input) {
        if (!validateBasicInput(input, 10)) {
            return false;
        }
        try {
            int value = Integer.parseInt(input.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Checks if input contains potentially dangerous characters.
     * 
     * @param input the input to check
     * @return true if input contains dangerous characters
     */
    public static boolean containsDangerousCharacters(String input) {
        if (input == null) {
            return false;
        }
        String dangerous = "<>\"';|&$()\\`";
        for (char c : dangerous.toCharArray()) {
            if (input.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }
}
