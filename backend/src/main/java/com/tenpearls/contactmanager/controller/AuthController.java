package com.tenpearls.contactmanager.controller;

import com.tenpearls.contactmanager.dto.*;
import com.tenpearls.contactmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        UserResponse userResponse = userService.register(registerRequest);
        
        // Auto-login after registration by generating token
        LoginRequest loginRequest = new LoginRequest(
                registerRequest.getEmail() != null ? registerRequest.getEmail() : registerRequest.getPhone(),
                registerRequest.getPassword()
        );
        AuthResponse authResponse = userService.login(loginRequest);
        
        ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();
                
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userResponse);
    }

    /**
     * Authenticates a user and generates a JWT.
     *
     * @param loginRequest the user login credentials
     * @return the authentication response containing JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        AuthResponse authResponse = userService.login(loginRequest);
        
        ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();
                
        // We still return the AuthResponse (or just User info) so frontend can use it if needed, but it shouldn't store the token
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    /**
     * Logs out the user by clearing the JWT cookie.
     *
     * @param response the HTTP response to clear the cookie
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Lax")
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully");
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
