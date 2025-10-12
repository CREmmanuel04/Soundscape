package com.example.soundscape.services;

import com.example.soundscape.auth.SoundscapeUserPrincipal;
import com.example.soundscape.models.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Custom OAuth2 service that converts Spotify OAuth data into our unified principal
 * This is where the magic happens - OAuth users become Soundscape users!
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // First, let Spring Security get the OAuth2 user data from Spotify
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Extract Spotify user data
        Map<String, Object> attributes = oauth2User.getAttributes();
        String spotifyUserId = (String) attributes.get("id");
        String displayName = (String) attributes.get("display_name");
        String email = (String) attributes.get("email");

        System.out.println("=== OAUTH2 USER LOAD ===");
        System.out.println("Spotify User ID: " + spotifyUserId);
        System.out.println("Display Name: " + displayName);
        System.out.println("Email: " + email);

        // Find or create user based on Spotify data
        User user = findOrCreateUserFromOAuth(spotifyUserId, displayName, email);

        // Return our unified principal with OAuth attributes
        return new SoundscapeUserPrincipal(user, attributes);
    }

    private User findOrCreateUserFromOAuth(String spotifyUserId, String displayName, String email) {
        // First, try to find user by Spotify ID (already linked)
        User user = userService.findBySpotifyUserId(spotifyUserId);

        if (user != null) {
            System.out.println("Found existing user by Spotify ID: " + user.getUsername());
            return user;
        }

        // If not found by Spotify ID, try to find by email
        if (email != null) {
            user = userService.findByEmail(email);
            if (user != null) {
                System.out.println("Found existing user by email, linking Spotify: " + user.getUsername());
                // Link Spotify to existing user
                user.setSpotifyUserId(spotifyUserId);
                user.setSpotifyDisplayName(displayName);
                user.setSpotifyConnectedAt(Instant.now());
                return userService.save(user);
            }
        }

        // If no existing user found, create a new one
        // For demo purposes, we'll create a placeholder user
        // In a real app, you might redirect to a registration page
        System.out.println("No existing user found - would create new user for Spotify");

        // For now, throw exception to prevent auto-creation
        throw new OAuth2AuthenticationException("No existing user found for Spotify account. Please register first.");
    }
}