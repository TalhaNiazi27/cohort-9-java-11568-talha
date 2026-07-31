package com.tenpearls.contactmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a Contact card owned by a User.
 */
@Entity
@Table(name = "contacts")
@Getter
@Setter
@ToString(exclude = {"user", "emails", "phones"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    /**
     * Unique identifier for the contact.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The User who owns this contact card.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * First name of the contact.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Last name of the contact.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Job title or designation of the contact.
     */
    private String title;

    /**
     * List of emails associated with this contact.
     */
    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Email> emails = new ArrayList<>();

    /**
     * List of phone numbers associated with this contact.
     */
    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Phone> phones = new ArrayList<>();

    /**
     * Timestamp of when the contact was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last time the contact was updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lifecycle callback method to set creation and update timestamps before persist.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Lifecycle callback method to update the timestamp before merge.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact other = (Contact) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
