package com.tenpearls.contactmanager.repository;

import com.tenpearls.contactmanager.model.Contact;
import com.tenpearls.contactmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private Contact savedContact;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .phone("1234567890")
                .password("encoded_password")
                .build();
        savedUser = userRepository.save(user);

        Contact contact = Contact.builder()
                .user(savedUser)
                .firstName("John")
                .lastName("Doe")
                .title("Developer")
                .build();
        savedContact = contactRepository.save(contact);
    }

    @Test
    void findByUser_ReturnsPaginatedContacts() {
        Page<Contact> result = contactRepository.findByUser(savedUser, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void findByIdAndUser_ReturnsContact() {
        Optional<Contact> result = contactRepository.findByIdAndUser(savedContact.getId(), savedUser);

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void findByIdAndUser_InvalidUser_ReturnsEmpty() {
        User otherUser = User.builder()
                .firstName("Other")
                .lastName("User")
                .email("other@example.com")
                .phone("0987654321")
                .password("encoded_password")
                .build();
        userRepository.save(otherUser);

        Optional<Contact> result = contactRepository.findByIdAndUser(savedContact.getId(), otherUser);

        assertThat(result).isEmpty();
    }
}
