package com.tenpearls.contactmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for handling the result of a registration operation.
 * Contains both the user details and the authentication response (with token)
 * so the client can automatically log in.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private UserResponse userResponse;
    private AuthResponse authResponse;
}
