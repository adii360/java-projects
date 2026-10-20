package com.ecommerce.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("john.doe@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user+test@domain.co.uk"));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@missingusername.com"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail(""));
    }

    @Test
    public void testSanitizationPreventsXSS() {
        String dangerousInput = "<script>alert('xss');</script>&\"'";
        String sanitized = ValidationUtil.sanitize(dangerousInput);
        assertFalse(sanitized.contains("<script>"));
        assertTrue(sanitized.contains("&lt;script&gt;"));
        assertTrue(sanitized.contains("&amp;"));
        assertTrue(sanitized.contains("&quot;"));
    }

    @Test
    public void testSlugGeneration() {
        assertEquals("galaxy-s25-ultra", ValidationUtil.toSlug("Galaxy S25 Ultra"));
        assertEquals("pro-macbook-16-inch", ValidationUtil.toSlug("Pro MacBook 16-inch!"));
    }

    @Test
    public void testNumericParsers() {
        assertEquals(42, ValidationUtil.parseIntOrDefault("42", 0));
        assertEquals(10, ValidationUtil.parseIntOrDefault("invalid", 10));

        assertEquals(new BigDecimal("99.99"), ValidationUtil.parseBigDecimalOrDefault("99.99", BigDecimal.ZERO));
        assertEquals(BigDecimal.ONE, ValidationUtil.parseBigDecimalOrDefault("bad", BigDecimal.ONE));
    }
}
