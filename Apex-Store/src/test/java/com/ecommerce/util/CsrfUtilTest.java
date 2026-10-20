package com.ecommerce.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CsrfUtilTest {

    // Simple test stub for HttpSession
    static class FakeHttpSession implements HttpSession {
        private final Map<String, Object> attributes = new HashMap<>();

        @Override public Object getAttribute(String s) { return attributes.get(s); }
        @Override public void setAttribute(String s, Object o) { attributes.put(s, o); }
        @Override public void removeAttribute(String s) { attributes.remove(s); }
        @Override public long getCreationTime() { return 0; }
        @Override public String getId() { return "test-session-id"; }
        @Override public long getLastAccessedTime() { return 0; }
        @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
        @Override public void setMaxInactiveInterval(int i) {}
        @Override public int getMaxInactiveInterval() { return 0; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return java.util.Collections.enumeration(attributes.keySet()); }
        @Override public void invalidate() { attributes.clear(); }
        @Override public boolean isNew() { return false; }
    }

    @Test
    public void testTokenGenerationIsNotNullAndConsistentInSameSession() {
        HttpSession session = new FakeHttpSession();
        String token1 = CsrfUtil.getToken(session);

        assertNotNull(token1);
        assertFalse(token1.trim().isEmpty());

        String token2 = CsrfUtil.getToken(session);
        assertEquals(token1, token2, "Token should remain identical across requests in same session");
    }

    @Test
    public void testNullSessionReturnsEmptyString() {
        assertEquals("", CsrfUtil.getToken(null));
    }
}
