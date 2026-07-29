package com.tenpearls.contactmanager.controller;

import com.tenpearls.contactmanager.dto.ContactRequest;
import com.tenpearls.contactmanager.dto.ContactResponse;
import com.tenpearls.contactmanager.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller handling REST API endpoints for user Contact management.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    /**
     * Constructs the ContactController with required ContactService.
     *
     * @param contactService the contact service handling business logic
     */
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Creates a new contact associated with the authenticated user.
     *
     * @param principal      the authenticated principal context
     * @param contactRequest the details of the contact to create
     * @return the created contact details response
     */
    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            Principal principal,
            @Valid @RequestBody ContactRequest contactRequest) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ContactResponse response = contactService.createContact(contactRequest, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
