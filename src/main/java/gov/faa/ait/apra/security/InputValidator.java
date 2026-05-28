/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * STIG V-220631: Whitelist-based input validation for all user-supplied parameters.
 * All inputs are validated against known-good patterns before processing.
 */
public final class InputValidator {

    private static final int MAX_INPUT_LENGTH = 255;
    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-. ]+$");
    private static final Pattern EDITION_PATTERN = Pattern.compile("^(current|next|changeset)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^(pdf|tiff|zip)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern GEONAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,'\\-]+$");
    private static final Pattern SERIES_PATTERN = Pattern.compile("^(low|high|area)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VOLUME_PATTERN = Pattern.compile(
        "^(US|NORTHWEST|SOUTHWEST|NORTH CENTRAL|SOUTH CENTRAL|EAST CENTRAL|SOUTHEAST|NORTHEAST|PACIFIC|ALASKA)$",
        Pattern.CASE_INSENSITIVE);

    private static final Set<String> VALID_GEONAMES;
    static {
        VALID_GEONAMES = new HashSet<>(Arrays.asList(
            "US", "ALASKA", "PACIFIC", "CARIBBEAN",
            "ALBUQUERQUE", "ANCHORAGE", "ATLANTA", "BETHEL", "BILLINGS", "BROWNSVILLE",
            "CAPE LISBURNE", "CHARLOTTE", "CHEYENNE", "CHICAGO", "CINCINNATI", "COLD BAY",
            "DALLAS-FT WORTH", "DAWSON", "DENVER", "DETROIT", "DUTCH HARBOR", "EL PASO",
            "FAIRBANKS", "GREAT FALLS", "GREEN BAY", "HALIFAX", "HAWAIIAN ISLANDS", "HOUSTON",
            "JACKSONVILLE", "JUNEAU", "KANSAS CITY", "KETCHIKAN", "KLAMATH FALLS", "KODIAK",
            "LAKE HURON", "LAS VEGAS", "LOS ANGELES", "MCGRATH", "MEMPHIS", "MIAMI", "MONTREAL",
            "NEW ORLEANS", "NEW YORK", "NOME", "OMAHA", "PHOENIX", "POINT BARROW",
            "SALT LAKE CITY", "SAN ANTONIO", "SAN FRANCISCO", "SEATTLE", "SEWARD", "ST LOUIS",
            "TWIN CITIES", "WASHINGTON", "WESTERN ALEUTIAN ISLANDS", "WHITEHORSE", "WICHITA",
            "ANCHORAGE-FAIRBANKS", "BALTIMORE-WASHINGTON", "BOSTON", "CLEVELAND",
            "DALLAS-FT WORTH", "DENVER-COLORADO SPRINGS", "MINNEAPOLIS-ST PAUL",
            "PHILADELPHIA", "PITTSBURGH", "PUERTO RICO-VI", "SAN DIEGO", "TAMPA-ORLANDO",
            "GRAND_CANYON", "GRAND CANYON",
            "BALTIMORE WASHINGTON HELI", "BOSTON HELI", "CHICAGO HELI",
            "DALLAS FT. WORTH HELI", "DETROIT HELI", "HOUSTON HELI",
            "LOS ANGELES HELI", "NEW YORK HELI", "U.S GULF COAST"
        ));
    }

    private InputValidator() { }

    /**
     * Validate a generic string input against maximum length and safe characters.
     */
    public static boolean validateString(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (value.length() > maxLength) {
            return false;
        }
        return ALPHA_NUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Validate the edition parameter (current, next, or changeset).
     */
    public static boolean validateEdition(String edition) {
        if (edition == null || edition.isEmpty()) {
            return true; // null/empty defaults to "current"
        }
        if (edition.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        return EDITION_PATTERN.matcher(edition).matches();
    }

    /**
     * Validate the format parameter (pdf, tiff, or zip).
     */
    public static boolean validateFormat(String format) {
        if (format == null || format.isEmpty()) {
            return true; // null/empty defaults to "pdf"
        }
        if (format.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        return FORMAT_PATTERN.matcher(format).matches();
    }

    /**
     * Validate a geoname parameter against the whitelist of known cities/regions.
     */
    public static boolean validateGeoname(String geoname) {
        if (geoname == null || geoname.isEmpty()) {
            return true; // null/empty is allowed for some endpoints
        }
        if (geoname.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        if (!GEONAME_PATTERN.matcher(geoname).matches()) {
            return false;
        }
        return VALID_GEONAMES.contains(geoname.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Validate a geoname that may accept US state names.
     * Uses a looser check since state names are validated downstream by USStateReferenceData.
     */
    public static boolean validateGeonameLoose(String geoname) {
        if (geoname == null || geoname.isEmpty()) {
            return true;
        }
        if (geoname.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        return GEONAME_PATTERN.matcher(geoname).matches();
    }

    /**
     * Validate the series type parameter (low, high, area).
     */
    public static boolean validateSeriesType(String seriesType) {
        if (seriesType == null || seriesType.isEmpty()) {
            return false;
        }
        if (seriesType.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        return SERIES_PATTERN.matcher(seriesType).matches();
    }

    /**
     * Validate the volume parameter for supplement charts.
     */
    public static boolean validateVolume(String volume) {
        if (volume == null || volume.isEmpty()) {
            return true; // null/empty defaults to US
        }
        if (volume.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        return VOLUME_PATTERN.matcher(volume).matches();
    }
}
