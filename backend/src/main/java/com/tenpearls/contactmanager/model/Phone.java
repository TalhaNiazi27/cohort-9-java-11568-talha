package com.tenpearls.contactmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity representing a Phone number linked to a Contact.
 */
@Entity
@Table(name = "phones")
@Getter
@Setter
@ToString(exclude = "contact")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phone {

    /**
     * Unique identifier for the phone record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * The Contact this phone number belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    @JsonIgnore
    private Contact contact;

    /**
     * The actual phone number string.
     */
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * Label for the phone number (e.g. "Work", "Home", "Mobile").
     */
    @Column(nullable = false)
    private String label;
}
