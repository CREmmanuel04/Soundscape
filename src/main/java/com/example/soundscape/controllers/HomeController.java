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
                    // Handle OAuth user - try to find by username first
                    String username = authentication.getName();
                    try {
                        user = userService.findByUsername(username);
                    } catch (RuntimeException e) {
                        // If user not found by username, it's a pure OAuth user
                        System.out.println("Pure OAuth user detected: " + username);
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
        System.out.println("=== SPOTIFY SUCCESS ===");
        System.out.println("Spotify User ID: " + authentication.getPrincipal().getAttribute("id"));
        System.out.println("Display Name: " + authentication.getPrincipal().getAttribute("display_name"));
        System.out.println("All Attributes: " + authentication.getPrincipal().getAttributes());

        return "redirect:/?spotifyTest=success";
    }
}