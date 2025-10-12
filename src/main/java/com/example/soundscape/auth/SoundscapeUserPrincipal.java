package com.example.soundscape.auth;

import com.example.soundscape.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Unified principal that works for both traditional authentication and OAuth2
 * This wraps our actual User entity and provides consistent access regardless of auth method
 */
public class SoundscapeUserPrincipal implements UserDetails, OAuth2User {

    private final User user;
    private Map<String, Object> oauth2Attributes;

    // Constructor for traditional authentication
    public SoundscapeUserPrincipal(User user) {
        this.user = user;
        this.oauth2Attributes = Collections.emptyMap();
    }

    // Constructor for OAuth2 authentication
    public SoundscapeUserPrincipal(User user, Map<String, Object> oauth2Attributes) {
        this.user = user;
        this.oauth2Attributes = oauth2Attributes;
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return oauth2Attributes;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    // Custom methods to access our User entity
    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    public boolean isSpotifyConnected() {
        return user.getSpotifyUserId() != null;
    }

    // Helper method to check if this is an OAuth user
    public boolean isOAuthUser() {
        return oauth2Attributes != null && !oauth2Attributes.isEmpty();
    }
}