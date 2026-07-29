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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    /**
     * Unique identifier for the email record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o)) return false;
        Email other = (Email) o;
        if (this.id == null || other.id == null) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return org.hibernate.Hibernate.getClass(this).hashCode();
    }
}
