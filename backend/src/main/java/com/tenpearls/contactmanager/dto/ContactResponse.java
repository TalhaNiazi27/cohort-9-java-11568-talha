package com.tenpearls.contactmanager.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing a Contact record response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {

    /**
     * Unique identifier for the contact card.
     */
    private Long id;

    /**
     * First name of the contact.
     */
    private String firstName;

    /**
     * Last name of the contact.
     */
    private String lastName;

    /**
     * Job title or designation of the contact.
     */
    private String title;

    /**
     * List of emails associated with this contact.
     */
    @Builder.Default
    private List<EmailResponse> emails = new ArrayList<>();

    /**
     * List of phone numbers associated with this contact.
     */
    @Builder.Default
    private List<PhoneResponse> phones = new ArrayList<>();

    /**
     * Timestamp of when the contact record was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last time the contact was updated.
     */
    private LocalDateTime updatedAt;
}
