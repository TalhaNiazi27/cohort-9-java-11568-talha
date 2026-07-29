package com.tenpearls.contactmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity representing an Email address linked to a Contact.
 */
@Entity
@Table(name = "emails")
@Getter
@Setter
@ToString(exclude = "contact")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    /**
     * Unique identifier for the email record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * The Contact this email address belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    @JsonIgnore
    private Contact contact;

    /**
     * The actual email address string.
     */
    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    /**
     * Label for the email (e.g. "Work", "Home", "Personal").
     */
    @Column(nullable = false)
    private String label;
}
