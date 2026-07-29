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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phone {

    /**
     * Unique identifier for the phone record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone other = (Phone) o;
        if (this.id == null || other.id == null) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
