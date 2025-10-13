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

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== STARTING OAUTH2 USER LOAD ===");

        try {
            // First, let Spring Security get the OAuth2 user data from Spotify
            OAuth2User oauth2User = super.loadUser(userRequest);

            // Extract Spotify user data
            Map<String, Object> attributes = oauth2User.getAttributes();
            String spotifyUserId = (String) attributes.get("id");
            String displayName = (String) attributes.get("display_name");
            String email = (String) attributes.get("email");

            System.out.println("=== OAUTH2 USER DATA RECEIVED ===");
            System.out.println("Spotify User ID: " + spotifyUserId);
            System.out.println("Display Name: " + displayName);
            System.out.println("Email: " + email);
            System.out.println("All attributes: " + attributes.keySet());

            // Find or create user based on Spotify data
            User user = findOrCreateUserFromOAuth(spotifyUserId, displayName, email);

            System.out.println("=== USER PROCESSING COMPLETE ===");
            System.out.println("Final user: " + user.getUsername() + " (ID: " + user.getId() + ")");

            // Return our unified principal with OAuth attributes
            return new SoundscapeUserPrincipal(user, attributes);
        } catch (Exception e) {
            System.err.println("=== ERROR IN OAUTH2 USER LOAD ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private User findOrCreateUserFromOAuth(String spotifyUserId, String displayName, String email) {
        System.out.println("=== FINDING OR CREATING USER ===");

        // First, try to find user by Spotify ID (already linked)
        User user = userService.findBySpotifyUserId(spotifyUserId);
        System.out.println("Search by Spotify ID '" + spotifyUserId + "': " + (user != null ? "FOUND" : "NOT FOUND"));

        if (user != null) {
            System.out.println("Found existing user by Spotify ID: " + user.getUsername());
            return user;
        }

        // If not found by Spotify ID, try to find by email
        if (email != null) {
            user = userService.findByEmail(email);
            System.out.println("Search by email '" + email + "': " + (user != null ? "FOUND" : "NOT FOUND"));
            if (user != null) {
                System.out.println("Found existing user by email, linking Spotify: " + user.getUsername());
                // Link Spotify to existing user
                user.setSpotifyUserId(spotifyUserId);
                user.setSpotifyDisplayName(displayName);
                user.setSpotifyConnectedAt(Instant.now());
                User savedUser = userService.save(user);
                System.out.println("Linked Spotify to existing user: " + savedUser.getUsername());
                return savedUser;
            }
        }

        // If no existing user found, create a new one with Spotify data
        System.out.println("No existing user found - creating new user from Spotify OAuth...");

        try {
            // Generate a unique username based on Spotify display name
            String username = generateUniqueUsername(displayName);
            System.out.println("Generated username: " + username);

            // Create new user - we'll use a placeholder password since OAuth users don't need it
            User newUser = new User(username, email, "OAUTH_USER_NO_PASSWORD");
            newUser.setSpotifyUserId(spotifyUserId);
            newUser.setSpotifyDisplayName(displayName);
            newUser.setSpotifyConnectedAt(Instant.now());

            System.out.println("About to save new user: " + newUser.getUsername());
            User savedUser = userService.save(newUser);
            System.out.println("Successfully created new user: " + savedUser.getUsername() + " (ID: " + savedUser.getId() + ")");

            return savedUser;
        } catch (Exception e) {
            System.err.println("ERROR creating user: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("User creation failed: " + e.getMessage());
        }
    }

    private String generateUniqueUsername(String displayName) {
        // Clean the display name to create a valid username
        String baseUsername = displayName != null ?
                displayName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() : "spotifyuser";

        // If base username is empty, use a default
        if (baseUsername.isEmpty()) {
            baseUsername = "spotifyuser";
        }

        String username = baseUsername;
        int counter = 1;

        // Keep trying until we find a unique username
        while (userService.existsByUsername(username)) {
            System.out.println("Username '" + username + "' exists, trying alternative...");
            username = baseUsername + counter;
            counter++;
        }

        System.out.println("Final generated username: " + username);
        return username;
    }
}