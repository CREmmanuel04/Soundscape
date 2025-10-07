package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
public class SpotifyLinkController {

    private final UserService userService;

    public SpotifyLinkController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/link-spotify")
    public String linkSpotifyAccount(OAuth2AuthenticationToken oauthToken, Authentication authentication) {
        System.out.println("=== LINKING SPOTIFY ===");

        if (oauthToken != null) {
            String spotifyUserId = oauthToken.getPrincipal().getAttribute("id");
            String displayName = oauthToken.getPrincipal().getAttribute("display_name");
            String email = oauthToken.getPrincipal().getAttribute("email");

            System.out.println("Spotify User ID: " + spotifyUserId);
            System.out.println("Display Name: " + displayName);
            System.out.println("Email: " + email);

            // Check if we already have a user with this Spotify ID
            User existingUserWithSpotify = userService.findBySpotifyUserId(spotifyUserId);
            if (existingUserWithSpotify != null) {
                System.out.println("Spotify account already linked to user: " + existingUserWithSpotify.getUsername());
                return "redirect:/?spotifyLinked=true";
            }

            // Try to find user by email (since we have jackrea100@gmail.com from Spotify)
            User userByEmail = userService.findByEmail(email);
            if (userByEmail != null) {
                // Link Spotify to the existing user by email
                userByEmail.setSpotifyUserId(spotifyUserId);
                userByEmail.setSpotifyDisplayName(displayName);
                // Fix the date conversion - use one of these options:
                userByEmail.setSpotifyConnectedAt(Instant.now()); // Option 1: Use Instant directly
                // OR
                // userByEmail.setSpotifyConnectedAt(LocalDateTime.now()); // Option 2: Use LocalDateTime if your entity supports it

                userService.save(userByEmail);
                System.out.println("Successfully linked Spotify to existing user by email: " + userByEmail.getUsername());
                return "redirect:/?spotifyLinked=true";
            }

            System.out.println("No existing user found to link with Spotify");
        }

        return "redirect:/?spotifyLinked=true";
    }
}