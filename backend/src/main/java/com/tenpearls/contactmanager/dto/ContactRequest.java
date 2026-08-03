package com.tenpearls.contactmanager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing a request to create or update a Contact.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactRequest {

    /**
     * First name of the contact.
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * Last name of the contact.
     */
    @NotBlank(message = "Last name is required")
    private String lastName;

    /**
     * Optional job title of the contact.
     */
    private String title;

    /**
     * Nested list of email addresses to link to this contact.
     */
    @Valid
    @Builder.Default
    private List<EmailRequest> emails = new ArrayList<>();

    /**
     * Nested list of phone numbers to link to this contact.
     */
    @Valid
    @Builder.Default
    private List<PhoneRequest> phones = new ArrayList<>();
}
