package com.tenpearls.contactmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a user account in the database.
 */
@Entity
@Table(name = "users")
@Getter
@ToString(exclude = "password")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user account.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Optional unique email address for registration and login.
     */
    @Setter
    private String email;

    /**
     * Optional unique phone number for registration and login.
     */
    @Setter
    private String phone;

    /**
     * Encrypted hashed password.
     */
    @Column(nullable = false)
    @Setter
    private String password;

    /**
     * Timestamp of when the user account was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Lifecycle callback method to set the creation timestamp before saving.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
