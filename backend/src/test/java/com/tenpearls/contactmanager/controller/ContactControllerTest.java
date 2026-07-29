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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true)
class ContactControllerTest {

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

    @Test
    @WithMockUser(username = "user@example.com")
    void createContact_Success() throws Exception {
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
    @WithMockUser(username = "user@example.com")
    void createContact_InvalidInput_Returns400() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .firstName("") // Invalid: blank
                .lastName("Doe")
                .build();

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(contactService, never()).createContact(any(ContactRequest.class), anyString());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getContact_Success() throws Exception {
        ContactResponse response = ContactResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .title("CEO")
                .build();

        when(contactService.getContact(eq(1L), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(contactService, times(1)).getContact(eq(1L), eq("user@example.com"));
    }

    @Test
    void getContact_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isUnauthorized());

        verify(contactService, never()).getContact(anyLong(), anyString());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getContact_NotFound_Returns404() throws Exception {
        when(contactService.getContact(eq(999L), eq("user@example.com")))
                .thenThrow(new com.tenpearls.contactmanager.exception.ResourceNotFoundException("Contact not found"));

        mockMvc.perform(get("/api/contacts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Contact not found"));

        verify(contactService, times(1)).getContact(eq(999L), eq("user@example.com"));
    }
}
