package com.tenpearls.contactmanager.dto;

import lombok.*;

/**
 * Data Transfer Object representing a Phone record response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneResponse {

    /**
     * Unique identifier for the phone record.
     */
    private Long id;

    /**
     * The phone number.
     */
    private String phoneNumber;

    /**
     * Label for the phone number (e.g. Mobile, Work).
     */
    private String label;
}
