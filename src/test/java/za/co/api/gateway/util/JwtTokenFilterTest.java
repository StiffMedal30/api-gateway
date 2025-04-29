package za.co.api.gateway.util;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtTokenFilterTest {

    private JwtTokenFilter jwtTokenFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenFilter = new JwtTokenFilter();
        jwtTokenFilter.setSECRET(Base64.getEncoder().encodeToString("m0Y%3jF1s9d7@Lz8qWeR!8u6TxCvBnMzm0Y%3jF1s9d7@Lz8qWeR!8u6TxCvBnMz".getBytes()));
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void testShouldNotFilter() throws ServletException {
        when(request.getServletPath()).thenReturn("/api/user/login");
        assertTrue(jwtTokenFilter.shouldNotFilter(request));

        when(request.getServletPath()).thenReturn("/api/user/register");
        assertTrue(jwtTokenFilter.shouldNotFilter(request));

        when(request.getServletPath()).thenReturn("/api/user/password/reset");
        assertTrue(jwtTokenFilter.shouldNotFilter(request));
    }

    @Test
    void testDoFilterInternalWithValidToken() throws Exception {
        String token = Jwts.builder().setSubject("test-user").signWith(getKey()).compact();
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid-token");

        jwtTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    private Key getKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(jwtTokenFilter.getSECRET()), "HmacSHA256");
    }
}