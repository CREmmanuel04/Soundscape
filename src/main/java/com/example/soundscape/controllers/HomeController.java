package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                User user;

                if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                    // Handle OAuth user - try to find by Spotify ID first
                    String spotifyUserId = oauthToken.getPrincipal().getAttribute("id");
                    String email = oauthToken.getPrincipal().getAttribute("email");

                    System.out.println("OAuth User - Spotify ID: " + spotifyUserId);
                    System.out.println("OAuth User - Email: " + email);

                    // First try to find user by Spotify ID
                    user = userService.findBySpotifyUserId(spotifyUserId);

                    if (user == null && email != null) {
                        // If no user found by Spotify ID, try by email
                        user = userService.findByEmail(email);
                        if (user != null) {
                            // Link the Spotify account immediately
                            user.setSpotifyUserId(spotifyUserId);
                            user.setSpotifyDisplayName(oauthToken.getPrincipal().getAttribute("display_name"));
                            user.setSpotifyConnectedAt(java.time.Instant.now());
                            userService.save(user);
                            System.out.println("Auto-linked Spotify during OAuth login: " + user.getUsername());
                        }
                    }

                    if (user == null) {
                        System.out.println("Pure OAuth user - no local account found");
                        user = null;
                    }
                } else {
                    // Regular username/password user
                    user = userService.findByUsername(authentication.getName());
                }

                model.addAttribute("user", user);
            } catch (RuntimeException e) {
                System.out.println("User not found: " + authentication.getName());
                // Continue without user object
            }
        }
        return "home";
    }

    @GetMapping("/spotify-success")
    public String spotifySuccess(OAuth2AuthenticationToken authentication) {
        System.out.println("=== SPOTIFY SUCCESS DEBUG ===");
        System.out.println("Spotify User ID: " + authentication.getPrincipal().getAttribute("id"));
        System.out.println("Display Name: " + authentication.getPrincipal().getAttribute("display_name"));
        System.out.println("All Attributes: " + authentication.getPrincipal().getAttributes());

        return "redirect:/?spotifyTest=success";
    }
}