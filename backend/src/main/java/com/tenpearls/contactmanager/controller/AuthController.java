package com.tenpearls.contactmanager.controller;

import com.tenpearls.contactmanager.dto.*;
import com.tenpearls.contactmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller for handling authentication requests such as registration, login,
 * retrieving current user details, and changing user passwords.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    /**
     * Constructs the AuthController with the required UserService.
     *
     * @param userService the user service handling authentication business logic
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     *
     * @param registerRequest the user registration details
     * @return the registered user details
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse response = userService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and generates a JWT.
     *
     * @param loginRequest the user login credentials
     * @return the authentication response containing JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile details of the authenticated user.
     *
     * @param principal the authenticated principal context
     * @return the authenticated user profile details
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Changes the password of the authenticated user.
     *
     * @param principal             the authenticated principal context
     * @param changePasswordRequest the current and new password details
     * @return a message indicating successful password change
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.changePassword(principal.getName(), changePasswordRequest);
        return ResponseEntity.ok("Password changed successfully");
    }
}
