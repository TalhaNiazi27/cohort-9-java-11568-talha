package com.tenpearls.contactmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that ensures the CSRF token is rendered in a cookie for Single Page Applications (SPAs).
 * Spring Security 6 defers the loading of the CsrfToken, so we must explicitly call getToken()
 * to force the token to be generated and the cookie to be sent to the frontend.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        // Render the token value to a cookie by causing the deferred token to be resolved
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        
        filterChain.doFilter(request, response);
    }
}
