package com.tenpearls.contactmanager.service;

import com.tenpearls.contactmanager.dto.*;
import com.tenpearls.contactmanager.exception.BadRequestException;
import com.tenpearls.contactmanager.exception.ResourceAlreadyExistsException;
import com.tenpearls.contactmanager.exception.UnauthorizedException;
import com.tenpearls.contactmanager.model.User;
import com.tenpearls.contactmanager.repository.UserRepository;
import com.tenpearls.contactmanager.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service implementation containing core business logic for user registration,
 * authentication, profile retrieval, and password updates.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Constructs UserServiceImpl with its required dependencies.
     *
     * @param userRepository        repository for database user transactions
     * @param passwordEncoder       encoder for hashing passwords
     * @param authenticationManager authentication manager to perform login validation
     * @param tokenProvider         provider for JWT token creation
     */
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Processing user registration request");

        // Validate that at least email or phone is present
        boolean hasEmail = StringUtils.hasText(request.getEmail());
        boolean hasPhone = StringUtils.hasText(request.getPhone());

        if (!hasEmail && !hasPhone) {
            throw new BadRequestException("At least email or phone number must be provided for registration");
        }

        // Check duplicate email
        if (hasEmail && userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already registered");
        }

        // Check duplicate phone
        if (hasPhone && userRepository.existsByPhone(request.getPhone())) {
            throw new ResourceAlreadyExistsException("Phone number is already registered");
        }

        // Create new user object
        User user = User.builder()
                .email(hasEmail ? request.getEmail() : null)
                .phone(hasPhone ? request.getPhone() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceAlreadyExistsException("Email or phone number is already registered");
        }

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing user login request");

        // Authenticate user via AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Find the user to get ID and details
        User user = findUserByUsername(request.getUsername());

        log.info("User authenticated successfully. Generating response for ID: {}", user.getId());

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = findUserByUsername(username);
        return mapToUserResponse(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findUserByUsername(username);
        log.info("Processing password change request for user ID: {}", user.getId());

        // Verify current password matches
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: current password incorrect for user ID {}", user.getId());
            throw new UnauthorizedException("Incorrect current password");
        }

        // Save new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", user.getId());
    }

    /**
     * Helper method to look up a user by email or phone number.
     *
     * @param username the email or phone number query
     * @return the user details entity
     * @throws UsernameNotFoundException if no user matches the email or phone number
     */
    private User findUserByUsername(String username) {
        return userRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Maps a User entity into a UserResponse DTO.
     *
     * @param user the user data model
     * @return the corresponding UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
