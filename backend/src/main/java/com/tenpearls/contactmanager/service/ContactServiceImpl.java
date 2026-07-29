package com.tenpearls.contactmanager.service;

import com.tenpearls.contactmanager.dto.*;
import com.tenpearls.contactmanager.exception.ResourceNotFoundException;
import com.tenpearls.contactmanager.model.*;
import com.tenpearls.contactmanager.repository.ContactRepository;
import com.tenpearls.contactmanager.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation containing business logic for Contact CRUD and search.
 */
@Service
@Slf4j
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    /**
     * Constructs ContactServiceImpl with required repository dependencies.
     *
     * @param contactRepository repository for database contact operations
     * @param userRepository    repository for database user lookups
     */
    public ContactServiceImpl(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ContactResponse createContact(ContactRequest request, String currentUserUsername) {
        log.info("Creating a new contact for user: {}", currentUserUsername);
        User owner = fetchOwner(currentUserUsername);

        Contact contact = Contact.builder()
                .user(owner)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .build();

        if (request.getEmails() != null) {
            for (EmailRequest emailReq : request.getEmails()) {
                contact.getEmails().add(Email.builder()
                        .contact(contact)
                        .emailAddress(emailReq.getEmailAddress())
                        .label(emailReq.getLabel())
                        .build());
            }
        }

        if (request.getPhones() != null) {
            for (PhoneRequest phoneReq : request.getPhones()) {
                contact.getPhones().add(Phone.builder()
                        .contact(contact)
                        .phoneNumber(phoneReq.getPhoneNumber())
                        .label(phoneReq.getLabel())
                        .build());
            }
        }

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact created successfully with ID: {} for user ID: {}", savedContact.getId(), owner.getId());
        return mapToContactResponse(savedContact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContact(Long id, String currentUserUsername) {
        log.info("Fetching contact ID: {} for user: {}", id, currentUserUsername);
        User owner = fetchOwner(currentUserUsername);
        Contact contact = fetchContactAndVerifyOwner(id, owner);
        return mapToContactResponse(contact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ContactResponse updateContact(Long id, ContactRequest request, String currentUserUsername) {
        log.info("Updating contact ID: {} for user: {}", id, currentUserUsername);
        User owner = fetchOwner(currentUserUsername);
        Contact contact = fetchContactAndVerifyOwner(id, owner);

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());

        // Update emails cleanly to handle orphan removal
        contact.getEmails().clear();
        if (request.getEmails() != null) {
            for (EmailRequest emailReq : request.getEmails()) {
                contact.getEmails().add(Email.builder()
                        .contact(contact)
                        .emailAddress(emailReq.getEmailAddress())
                        .label(emailReq.getLabel())
                        .build());
            }
        }

        // Update phones cleanly to handle orphan removal
        contact.getPhones().clear();
        if (request.getPhones() != null) {
            for (PhoneRequest phoneReq : request.getPhones()) {
                contact.getPhones().add(Phone.builder()
                        .contact(contact)
                        .phoneNumber(phoneReq.getPhoneNumber())
                        .label(phoneReq.getLabel())
                        .build());
            }
        }

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact ID: {} updated successfully", updatedContact.getId());
        return mapToContactResponse(updatedContact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteContact(Long id, String currentUserUsername) {
        log.info("Deleting contact ID: {} for user: {}", id, currentUserUsername);
        User owner = fetchOwner(currentUserUsername);
        Contact contact = fetchContactAndVerifyOwner(id, owner);
        contactRepository.delete(contact);
        log.info("Contact ID: {} deleted successfully", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> searchContacts(String search, int page, int size, String currentUserUsername) {
        log.info("Searching contacts with keyword: '{}', page: {}, size: {} for user: {}", search, page, size, currentUserUsername);
        User owner = fetchOwner(currentUserUsername);

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());

        Specification<Contact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Rule 1: Must belong to the current authenticated user
            predicates.add(cb.equal(root.get("user"), owner));

            // Rule 2: Optional text search criteria
            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";

                Predicate firstPredicate = cb.like(cb.lower(root.get("firstName")), searchLike);
                Predicate lastPredicate = cb.like(cb.lower(root.get("lastName")), searchLike);

                // Join with emails table
                Join<Contact, Email> emailJoin = root.join("emails", JoinType.LEFT);
                Predicate emailPredicate = cb.like(cb.lower(emailJoin.get("emailAddress")), searchLike);

                // Join with phones table
                Join<Contact, Phone> phoneJoin = root.join("phones", JoinType.LEFT);
                Predicate phonePredicate = cb.like(cb.lower(phoneJoin.get("phoneNumber")), searchLike);

                // Combine text filters with OR
                predicates.add(cb.or(firstPredicate, lastPredicate, emailPredicate, phonePredicate));
                
                // Group by contact ID to prevent duplicate records on joins
                if (query != null) {
                    query.distinct(true);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Contact> contactsPage = contactRepository.findAll(spec, pageable);
        List<ContactResponse> dtoList = contactsPage.getContent().stream()
                .map(this::mapToContactResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, contactsPage.getTotalElements());
    }

    private User fetchOwner(String username) {
        return userRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Owner account not found"));
    }

    private Contact fetchContactAndVerifyOwner(Long id, User owner) {
        return contactRepository.findByIdAndUser(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found or access denied"));
    }

    private ContactResponse mapToContactResponse(Contact contact) {
        List<EmailResponse> emails = contact.getEmails().stream()
                .map(e -> EmailResponse.builder()
                        .id(e.getId())
                        .emailAddress(e.getEmailAddress())
                        .label(e.getLabel())
                        .build())
                .collect(Collectors.toList());

        List<PhoneResponse> phones = contact.getPhones().stream()
                .map(p -> PhoneResponse.builder()
                        .id(p.getId())
                        .phoneNumber(p.getPhoneNumber())
                        .label(p.getLabel())
                        .build())
                .collect(Collectors.toList());

        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .title(contact.getTitle())
                .emails(emails)
                .phones(phones)
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}
