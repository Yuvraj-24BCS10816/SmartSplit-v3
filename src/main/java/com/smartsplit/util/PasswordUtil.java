package com.smartsplit.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Password hashing with PBKDF2-HMAC-SHA256 and legacy SHA-256 verification for v2 databases. */
public final class PasswordUtil {
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private PasswordUtil() { }

    public static String hash(String password) {
        if (password == null) throw new IllegalArgumentException("Password is required.");
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password.toCharArray(), salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + HEX.formatHex(salt) + "$" + HEX.formatHex(derived);
    }

    /** Verifies both current PBKDF2 hashes and the legacy v2 SHA-256 values. */
    public static boolean verify(String password, String encoded) {
        if (password == null || encoded == null || encoded.isBlank()) return false;
        try {
            if (encoded.startsWith("pbkdf2$")) {
                String[] parts = encoded.split("\\$", -1);
                if (parts.length != 4) return false;
                int iterations = Integer.parseInt(parts[1]);
                if (iterations < 100_000 || iterations > 2_000_000) return false;
                byte[] salt = HEX.parseHex(parts[2]);
                byte[] expected = HEX.parseHex(parts[3]);
                byte[] actual = derive(password.toCharArray(), salt, iterations);
                return MessageDigest.isEqual(expected, actual);
            }
            // v2 used an unsalted SHA-256 hex value; accepting it here allows upgrades without losing accounts.
            String legacy = legacySha256(password);
            boolean matches = MessageDigest.isEqual(
                    legacy.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    encoded.toLowerCase(java.util.Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return matches;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public static boolean isLegacyHash(String encoded) {
        return encoded != null && !encoded.startsWith("pbkdf2$");
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Password hashing is not available in this Java runtime.", ex);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static String legacySha256(String password) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HEX.formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is not available in this Java runtime.", ex);
        }
    }
}
