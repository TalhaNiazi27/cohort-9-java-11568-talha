package com.tenpearls.contactmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpearls.contactmanager.dto.ChangePasswordRequest;
import com.tenpearls.contactmanager.security.CustomUserDetailsService;
import com.tenpearls.contactmanager.security.JwtTokenProvider;
import com.tenpearls.contactmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.tenpearls.contactmanager.security.SecurityConfig;
import com.tenpearls.contactmanager.security.JwtAuthenticationFilter;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true) // Enable security filters to test actual security constraints
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getCurrentUser(anyString());
    }

    @Test
    void changePassword_Unauthenticated() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("password123", "newPassword123");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).changePassword(anyString(), any(ChangePasswordRequest.class));
    }
}
