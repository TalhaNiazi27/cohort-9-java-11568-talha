package com.tenpearls.contactmanager.security;

import com.tenpearls.contactmanager.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * UserDetails adapter that wraps the JPA User entity for Spring Security context.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * Constructs CustomUserDetails with the specified User entity.
     *
     * @param user the User entity from the database
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Gets the wrapped User entity.
     *
     * @return the User entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the unique ID of the user.
     *
     * @return the user ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return user.getEmail() != null ? user.getEmail() : user.getPhone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
