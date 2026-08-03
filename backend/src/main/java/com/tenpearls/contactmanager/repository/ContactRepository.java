package com.tenpearls.contactmanager.repository;

import com.tenpearls.contactmanager.model.Contact;
import com.tenpearls.contactmanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Contact persistence and dynamic search queries.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>, JpaSpecificationExecutor<Contact> {

    /**
     * Finds all contacts owned by a specific user with pagination.
     *
     * @param user     the user who owns the contacts
     * @param pageable pagination parameters
     * @return a page of contacts
     */
    Page<Contact> findByUser(User user, Pageable pageable);

    /**
     * Finds a contact by its ID and matching user owner to prevent horizontal privilege escalation.
     *
     * @param id   the contact ID
     * @param user the user who owns the contact
     * @return an Optional containing the Contact if found and owned, empty otherwise
     */
    Optional<Contact> findByIdAndUser(Long id, User user);
}
