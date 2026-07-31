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
import com.tenpearls.contactmanager.exception.ResourceNotFoundException;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;

@WebMvcTest(ContactController.class)
@Import(SecurityConfig.class)
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

    @Test
    void getContactById_CrossUserOwnership_ReturnsNotFound() throws Exception {
        mockAuthentication(); // Set up standard user@example.com mock authentication

        // When user@example.com tries to access a contact belonging to someone else, the service
        // should throw a ResourceNotFoundException (to avoid leaking existence).
        when(contactService.getContact(eq(1L), eq("user@example.com")))
                .thenThrow(new ResourceNotFoundException("Contact not found"));

        mockMvc.perform(get("/api/contacts/1")
                        .header("Authorization", "Bearer mock_token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Contact not found"));

        verify(contactService).getContact(1L, "user@example.com");
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getContact_InvalidIdType_Returns400() throws Exception {
        mockMvc.perform(get("/api/contacts/not-a-number")
                        .header("Authorization", "Bearer mock_token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void updateContact_Success() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .title("CTO")
                .build();

        ContactResponse response = ContactResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .title("CTO")
                .build();

        when(contactService.updateContact(eq(1L), any(ContactRequest.class), eq("user@example.com"))).thenReturn(response);

        mockMvc.perform(put("/api/contacts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.title").value("CTO"));

        verify(contactService, times(1)).updateContact(eq(1L), any(ContactRequest.class), eq("user@example.com"));
    }

    @Test
    void updateContact_Unauthenticated_Returns401() throws Exception {
        ContactRequest request = ContactRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .build();

        mockMvc.perform(put("/api/contacts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteContact_Success() throws Exception {
        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isNoContent());

        verify(contactService, times(1)).deleteContact(eq(1L), eq("user@example.com"));
    }

    @Test
    void deleteContact_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isUnauthorized());
    }
}
