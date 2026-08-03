package com.tenpearls.contactmanager.service;

import com.tenpearls.contactmanager.dto.ContactRequest;
import com.tenpearls.contactmanager.dto.ContactResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface defining operations for managing user contacts.
 */
public interface ContactService {

    /**
     * Creates a new contact for the authenticated user.
     *
     * @param request             the contact details to create
     * @param currentUserUsername the username (email or phone) of the authenticated owner
     * @return the created contact details response
     */
    ContactResponse createContact(ContactRequest request, String currentUserUsername);

    /**
     * Retrieves a contact by ID, ensuring it belongs to the authenticated user.
     *
     * @param id                  the ID of the contact
     * @param currentUserUsername the username (email or phone) of the authenticated owner
     * @return the contact details response
     */
    ContactResponse getContact(Long id, String currentUserUsername);

    /**
     * Updates an existing contact by ID, ensuring it belongs to the authenticated user.
     *
     * @param id                  the ID of the contact to update
     * @param request             the updated contact details
     * @param currentUserUsername the username (email or phone) of the authenticated owner
     * @return the updated contact details response
     */
    ContactResponse updateContact(Long id, ContactRequest request, String currentUserUsername);

    /**
     * Deletes a contact by ID, ensuring it belongs to the authenticated user.
     *
     * @param id                  the ID of the contact to delete
     * @param currentUserUsername the username (email or phone) of the authenticated owner
     */
    void deleteContact(Long id, String currentUserUsername);

    /**
     * Searches and lists contacts owned by the authenticated user with filtering and pagination.
     *
     * @param search              optional search keyword matching first name, last name, email, or phone
     * @param page                zero-indexed page number
     * @param size                number of items per page
     * @param currentUserUsername the username (email or phone) of the authenticated owner
     * @return a paginated list of matching contacts
     */
    Page<ContactResponse> searchContacts(String search, int page, int size, String currentUserUsername);
}
