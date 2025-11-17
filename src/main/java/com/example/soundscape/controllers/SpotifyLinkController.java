package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;

@Controller
public class SpotifyLinkController {

    private final UserService userService;

    public SpotifyLinkController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/link-spotify")
    public String linkSpotifyAccount(Authentication authentication, Model model) {

        // This page is the *success* page, so the user is already linked.
        // We just need to check and redirect.
        if (authentication instanceof OAuth2AuthenticationToken) {
            System.out.println("Spotify successfully linked.");
            model.addAttribute("connected", true);
        } else {
            System.out.println("Something went wrong, /link-spotify called without OAuth token.");
            model.addAttribute("connected", false);
        }

        // Redirect to the home page with a success message
        return "redirect:/?spotifyLinked=true";
    }
}