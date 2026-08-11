package com.tenpearls.contactmanager.service;

import com.tenpearls.contactmanager.dto.ContactRequest;
import com.tenpearls.contactmanager.dto.ContactResponse;
import com.tenpearls.contactmanager.dto.EmailRequest;
import com.tenpearls.contactmanager.dto.PhoneRequest;
import com.tenpearls.contactmanager.exception.ResourceNotFoundException;
import com.tenpearls.contactmanager.model.Contact;
import com.tenpearls.contactmanager.model.User;
import com.tenpearls.contactmanager.repository.ContactRepository;
import com.tenpearls.contactmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User mockUser;
    private Contact mockContact;
    private ContactRequest contactRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        mockContact = Contact.builder()
                .id(100L)
                .user(mockUser)
                .firstName("Jane")
                .lastName("Doe")
                .title("Manager")
                .build();

        contactRequest = ContactRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .title("Manager")
                .emails(Collections.singletonList(new EmailRequest("jane@example.com", "Work")))
                .phones(Collections.singletonList(new PhoneRequest("1234567890", "Mobile")))
                .build();
    }

    @Test
    void createContact_Success() {
        when(userRepository.findByEmailOrPhone("test@example.com", "test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(contactRepository.save(any(Contact.class))).thenReturn(mockContact);

        ContactResponse response = contactService.createContact(contactRequest, "test@example.com");

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void createContact_UserNotFound_ThrowsException() {
        when(userRepository.findByEmailOrPhone("unknown", "unknown"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                contactService.createContact(contactRequest, "unknown"));
    }

    @Test
    void getContact_Success() {
        when(userRepository.findByEmailOrPhone("test@example.com", "test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(contactRepository.findByIdAndUser(100L, mockUser))
                .thenReturn(Optional.of(mockContact));

        ContactResponse response = contactService.getContact(100L, "test@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Jane", response.getFirstName());
    }

    @Test
    void getContact_NotFound_ThrowsException() {
        when(userRepository.findByEmailOrPhone("test@example.com", "test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(contactRepository.findByIdAndUser(999L, mockUser))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                contactService.getContact(999L, "test@example.com"));
    }

    @Test
    void updateContact_Success() {
        when(userRepository.findByEmailOrPhone("test@example.com", "test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(contactRepository.findByIdAndUser(100L, mockUser))
                .thenReturn(Optional.of(mockContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(mockContact);

        ContactResponse response = contactService.updateContact(100L, contactRequest, "test@example.com");

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void deleteContact_Success() {
        when(userRepository.findByEmailOrPhone("test@example.com", "test@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(contactRepository.findByIdAndUser(100L, mockUser))
                .thenReturn(Optional.of(mockContact));

        contactService.deleteContact(100L, "test@example.com");

        verify(contactRepository, times(1)).delete(mockContact);
    }

    @Test
    void searchContacts_Success() {
        when(userRepository.findByEmailOrPhone("test@example.com", "test@example.com"))
                .thenReturn(Optional.of(mockUser));

        Page<Contact> mockPage = new PageImpl<>(Collections.singletonList(mockContact));
        when(contactRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(mockPage);

        Page<ContactResponse> response = contactService.searchContacts("Jane", 0, 10, "test@example.com");

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Jane", response.getContent().get(0).getFirstName());
    }
}
