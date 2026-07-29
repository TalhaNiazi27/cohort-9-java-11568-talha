package com.tenpearls.contactmanager.dto;

import lombok.*;

/**
 * Data Transfer Object representing an Email record response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResponse {

    /**
     * Unique identifier for the email record.
     */
    private Long id;

    /**
     * The email address.
     */
    private String emailAddress;

    /**
     * Label for the email (e.g. Work, Personal).
     */
    private String label;
}
