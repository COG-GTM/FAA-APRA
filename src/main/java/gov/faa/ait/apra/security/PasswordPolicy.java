/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * STIG V-220629 (NIST IA-5): Password policy enforcement.
 * Enforces minimum 14-character passwords with complexity requirements
 * (uppercase, lowercase, digit, special character). Provides secure
 * hashing using PBKDF2 with HMAC-SHA256 and 600,000 iterations.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 14;
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final int PBKDF2_ITERATIONS = 600000;
    private static final int HASH_LENGTH_BITS = 256;

    private PasswordPolicy() { }

    /**
     * Validate a password meets STIG requirements.
     * @return an error message, or null if the password is valid
     */
    public static String validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return "Password must be at least " + MIN_LENGTH + " characters";
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (SPECIAL_CHARS.indexOf(c) >= 0) hasSpecial = true;
        }
        if (!hasUpper) return "Must contain an uppercase letter";
        if (!hasLower) return "Must contain a lowercase letter";
        if (!hasDigit) return "Must contain a digit";
        if (!hasSpecial) return "Must contain a special character";
        return null;
    }

    /**
     * Hash a password using PBKDF2 with HMAC-SHA256 and a random salt.
     */
    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 not available", e);
        }
    }

    /**
     * Verify a password against a stored PBKDF2 hash.
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) return false;
        String[] parts = storedHash.split(":");
        if (parts.length != 2) return false;
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            return false;
        }
    }
}
