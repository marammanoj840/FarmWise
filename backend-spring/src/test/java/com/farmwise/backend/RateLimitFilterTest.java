package com.farmwise.backend.security;

import com.farmwise.backend.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        // 2 auth requests per minute, 5 api requests per minute for fast testing
        RateLimitProperties props = new RateLimitProperties(2, 5);
        filter = new RateLimitFilter(props);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void nonApiRequestsBypassRateLimiter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void allowsRequestsWithinLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void blocksRequestsExceedingLimitWith429() throws ServletException, IOException {
        String ip = "10.0.0.1";

        // Request 1 - ok
        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req1.setRemoteAddr(ip);
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilterInternal(req1, res1, filterChain);
        assertEquals(200, res1.getStatus());

        // Request 2 - ok
        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req2.setRemoteAddr(ip);
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, res2, filterChain);
        assertEquals(200, res2.getStatus());

        // Request 3 - rate limit exceeded!
        MockHttpServletRequest req3 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req3.setRemoteAddr(ip);
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        filter.doFilterInternal(req3, res3, filterChain);
        assertEquals(429, res3.getStatus());
    }

    @Test
    void honorsXForwardedForHeader() throws ServletException, IOException {
        MockHttpServletRequest req1 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req1.setRemoteAddr("127.0.0.1");
        req1.addHeader("X-Forwarded-For", "203.0.113.195, 10.0.0.1");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilterInternal(req1, res1, filterChain);
        assertEquals(200, res1.getStatus());

        MockHttpServletRequest req2 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req2.setRemoteAddr("127.0.0.1");
        req2.addHeader("X-Forwarded-For", "203.0.113.195, 10.0.0.1");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, res2, filterChain);
        assertEquals(200, res2.getStatus());

        MockHttpServletRequest req3 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req3.setRemoteAddr("127.0.0.1");
        req3.addHeader("X-Forwarded-For", "203.0.113.195, 10.0.0.1");
        MockHttpServletResponse res3 = new MockHttpServletResponse();
        filter.doFilterInternal(req3, res3, filterChain);
        assertEquals(429, res3.getStatus());
    }
}
