package com.tenpearls.contactmanager.service;

import com.tenpearls.contactmanager.dto.AuthResponse;
import com.tenpearls.contactmanager.dto.ChangePasswordRequest;
import com.tenpearls.contactmanager.dto.LoginRequest;
import com.tenpearls.contactmanager.dto.RegisterRequest;
import com.tenpearls.contactmanager.dto.RegistrationResponse;
import com.tenpearls.contactmanager.dto.UserResponse;

/**
 * Service interface defining user authentication, registration,
 * profile retrieval, and password change operations.
 */
public interface UserService {

    /**
     * Registers a new user account with an email or phone number.
     *
     * @param request the registration details
     * @return the registered user response details
     */
    RegistrationResponse register(RegisterRequest request);

    /**
     * Authenticates a user using their username (email or phone) and password.
     *
     * @param request the login request details
     * @return the authentication token response details
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieves the profile details of a user by their username (email or phone).
     *
     * @param username the email or phone number of the user
     * @return the user details response
     */
    UserResponse getCurrentUser(String username);

    /**
     * Changes the password of an existing user account.
     *
     * @param username the email or phone number of the user
     * @param request  the current and new password details
     */
    void changePassword(String username, ChangePasswordRequest request);
}
