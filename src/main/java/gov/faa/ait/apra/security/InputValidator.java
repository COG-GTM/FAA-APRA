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
 * STIG V-220631 (NIST SI-10): Whitelist-based input validation.
 * All user inputs are validated against known-good patterns with enforced length limits.
 */
public final class InputValidator {

    private static final int DEFAULT_MAX_LENGTH = 255;
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]+$");
    private static final Pattern GEONAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,'\\-]+$");
    private static final Pattern EDITION_PATTERN = Pattern.compile("^(current|next|changeset)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^(pdf|tiff|zip)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern STATE_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern VOLUME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]+$");

    private InputValidator() { }

    /**
     * Validate a generic string against maximum length.
     */
    public static boolean validateString(String value, int maxLength) {
        return value != null && !value.isEmpty() && value.length() <= maxLength;
    }

    /**
     * Validate a generic string with the default maximum length of 255.
     */
    public static boolean validateString(String value) {
        return validateString(value, DEFAULT_MAX_LENGTH);
    }

    /**
     * Validate an edition parameter (current, next, changeset).
     */
    public static boolean validateEdition(String edition) {
        if (edition == null || edition.isEmpty()) {
            return true;
        }
        return edition.length() <= 20 && EDITION_PATTERN.matcher(edition).matches();
    }

    /**
     * Validate a format parameter (pdf, tiff, zip).
     */
    public static boolean validateFormat(String format) {
        if (format == null || format.isEmpty()) {
            return true;
        }
        return format.length() <= 10 && FORMAT_PATTERN.matcher(format).matches();
    }

    /**
     * Validate a geoname parameter (city/region names).
     */
    public static boolean validateGeoname(String geoname) {
        if (geoname == null || geoname.isEmpty()) {
            return true;
        }
        return geoname.length() <= 100 && GEONAME_PATTERN.matcher(geoname).matches();
    }

    /**
     * Validate a US state name.
     */
    public static boolean validateState(String state) {
        if (state == null || state.isEmpty()) {
            return true;
        }
        return state.length() <= 50 && STATE_PATTERN.matcher(state).matches();
    }

    /**
     * Validate a volume identifier.
     */
    public static boolean validateVolume(String volume) {
        if (volume == null || volume.isEmpty()) {
            return true;
        }
        return volume.length() <= 50 && VOLUME_PATTERN.matcher(volume).matches();
    }

    /**
     * Validate an alphanumeric string with hyphens and underscores.
     */
    public static boolean validateAlphanumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.length() <= DEFAULT_MAX_LENGTH && ALPHANUMERIC_PATTERN.matcher(value).matches();
    }
}
