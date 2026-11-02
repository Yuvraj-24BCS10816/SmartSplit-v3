package com.smartsplit;

import com.smartsplit.service.AuthService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceValidationTest {
    private final AuthService service = new AuthService();

    @Test
    void rejectsUsernameShorterThanThreeCharacters() {
        assertThrows(IllegalArgumentException.class, () ->
                service.register("Sample Student", "ab", "sample@example.com", "strongpass123"));
    }

    @Test
    void rejectsMalformedEmailAddress() {
        assertThrows(IllegalArgumentException.class, () ->
                service.register("Sample Student", "sample_user", "not-an-email", "strongpass123"));
    }

    @Test
    void rejectsShortPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                service.register("Sample Student", "sample_user", "sample@example.com", "short"));
    }

    @Test
    void rejectsMissingName() {
        assertThrows(IllegalArgumentException.class, () ->
                service.register("  ", "sample_user", "sample@example.com", "strongpass123"));
    }
}
