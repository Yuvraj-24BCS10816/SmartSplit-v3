package com.smartsplit;

import com.smartsplit.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void pbkdf2HashVerifiesCorrectPassword() {
        String encoded = PasswordUtil.hash("correct horse battery");
        assertTrue(encoded.startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verify("correct horse battery", encoded));
        assertFalse(PasswordUtil.verify("wrong password", encoded));
    }

    @Test
    void eachHashUsesAUniqueSalt() {
        assertNotEquals(PasswordUtil.hash("same password"), PasswordUtil.hash("same password"));
    }

    @Test
    void legacySha256PasswordsRemainVerifiableForUpgrade() throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest("demo123".getBytes(StandardCharsets.UTF_8));
        String legacy = HexFormat.of().formatHex(hash);
        assertTrue(PasswordUtil.verify("demo123", legacy));
        assertTrue(PasswordUtil.isLegacyHash(legacy));
        assertFalse(PasswordUtil.verify("incorrect", legacy));
    }
}
