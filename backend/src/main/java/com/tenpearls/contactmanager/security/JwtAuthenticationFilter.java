package com.tenpearls.contactmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Request filter that extracts JWT token, validates it, loads the user,
 * and sets authentication details in the security context.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructs JwtAuthenticationFilter with provider and user details service.
     *
     * @param tokenProvider            token provider
     * @param customUserDetailsService custom user details service
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromJWT(jwt);

                if (StringUtils.hasText(username)) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.debug("JWT has no usable subject claim");
                }
            }
        } catch (UsernameNotFoundException ex) {
            log.error("Authentication failed: user no longer exists");
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            log.error("Unexpected authentication infrastructure failure");
            SecurityContextHolder.clearContext();
            throw new ServletException("Unexpected authentication infrastructure failure", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the request.
     *
     * @param request the HTTP request
     * @return the JWT token string, or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Try to extract from cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 2. Fallback to Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
