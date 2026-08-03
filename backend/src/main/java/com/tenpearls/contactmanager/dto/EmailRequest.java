package com.tenpearls.contactmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Object representing a request to link an email address to a contact.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {

    /**
     * The email address to associate.
     */
    @NotBlank(message = "Email address cannot be blank")
    @Email(message = "Invalid email address format")
    private String emailAddress;

    /**
     * Label for the email (e.g. Work, Personal).
     */
    @NotBlank(message = "Email label cannot be blank")
    private String label;
}
