package com.tenpearls.contactmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Object representing a request to link a phone number to a contact.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneRequest {

    /**
     * The phone number to associate.
     */
    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    /**
     * Label for the phone number (e.g. Mobile, Work, Home).
     */
    @NotBlank(message = "Phone label cannot be blank")
    private String label;
}
