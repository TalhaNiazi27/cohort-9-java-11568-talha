package com.tenpearls.contactmanager.repository;

import com.tenpearls.contactmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing User persistence and database queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address query
     * @return an Optional containing the User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their phone number.
     *
     * @param phone the phone number query
     * @return an Optional containing the User if found, empty otherwise
     */
    Optional<User> findByPhone(String phone);

    /**
     * Finds a user by their email or phone number.
     *
     * @param email the email query
     * @param phone the phone query
     * @return an Optional containing the User if found, empty otherwise
     */
    java.util.List<User> findAllByEmailOrPhone(String email, String phone);

    /**
     * Finds a single user by email or phone. 
     * Throws an exception if multiple users are found due to ambiguous identifiers.
     */
    default Optional<User> findByEmailOrPhone(String email, String phone) {
        java.util.List<User> users = findAllByEmailOrPhone(email, phone);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        if (users.size() > 1) {
            throw new org.springframework.dao.DataIntegrityViolationException("Ambiguous identifier: matches multiple distinct users");
        }
        return Optional.of(users.get(0));
    }

    /**
     * Checks if a user exists with the specified email address.
     *
     * @param email the email address check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the specified phone number.
     *
     * @param phone the phone number check
     * @return true if exists, false otherwise
     */
    boolean existsByPhone(String phone);
}
