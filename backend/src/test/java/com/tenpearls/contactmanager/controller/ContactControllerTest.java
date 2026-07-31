package com.tenpearls.contactmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpearls.contactmanager.dto.*;
import com.tenpearls.contactmanager.security.*;
import com.tenpearls.contactmanager.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;

@WebMvcTest(ContactController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = true)
class ContactControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContactService contactService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    private void mockAuthentication() {
        when(tokenProvider.validateToken(anyString())).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT(anyString())).thenReturn("user@example.com");

        org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("user@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        when(customUserDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    }

    @Test
    void createContact_Success() throws Exception {
        mockAuthentication();

        ContactRequest request = ContactRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .title("CEO")
                .emails(Collections.singletonList(new EmailRequest("john.doe@example.com", "Work")))
                .phones(Collections.singletonList(new PhoneRequest("1234567890", "Mobile")))
                .build();

        ContactResponse response = ContactResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .title("CEO")
                .emails(Collections.singletonList(new EmailResponse(1L, "john.doe@example.com", "Work")))
                .phones(Collections.singletonList(new PhoneResponse(1L, "1234567890", "Mobile")))
                .build();

        when(contactService.createContact(any(ContactRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer mock_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.title").value("CEO"))
                .andExpect(jsonPath("$.emails[0].emailAddress").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phones[0].phoneNumber").value("1234567890"));

        verify(contactService, times(1)).createContact(any(ContactRequest.class), eq("user@example.com"));
    }

    @Test
    void createContact_Unauthenticated_Returns401() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(contactService, never()).createContact(any(ContactRequest.class), anyString());
    }

    @Test
    void createContact_InvalidInput_Returns400() throws Exception {
        mockAuthentication();

        ContactRequest request = ContactRequest.builder()
                .firstName("") // Invalid: blank
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer mock_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request (Validation Failed)"))
                .andExpect(jsonPath("$.message").value("First name is required"));

        verify(contactService, never()).createContact(any(ContactRequest.class), anyString());
    }
}
