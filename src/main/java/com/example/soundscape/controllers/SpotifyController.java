package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import com.example.soundscape.services.SpotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class SpotifyController {

    private final UserRepository userRepository;
    private final SpotifyService spotifyService;

    public SpotifyController(UserRepository userRepository, SpotifyService spotifyService) {
        this.userRepository = userRepository;
        this.spotifyService = spotifyService;
    }

    // Page to connect Spotify account
    @GetMapping("/connect-spotify")
    public String showConnectSpotifyPage() {
        return "connect-spotify"; // Will create this template
    }

    // Handle Spotify connection - redirects to Spotify OAuth
    @PostMapping("/connect-spotify")
    public String connectSpotify() {
        // Spring Security will automatically handle the OAuth2 redirect
        return "redirect:/oauth2/authorization/spotify";
    }

    // Handle Spotify connection success
    /*
    @GetMapping("/spotify-success")
    public String handleSpotifySuccess(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if user has Spotify connected (we'll add this field to User)
            if (user.getSpotifyAccessToken() != null) {
                // Get user's top artists from Spotify
                String topArtists = spotifyService.getUserTopArtists(user.getSpotifyAccessToken());
                model.addAttribute("connected", true);
                model.addAttribute("topArtists", topArtists);
            } else {
                model.addAttribute("connected", false);
            }
        }

        return "spotify-success";
    }
     */

    // Find music matches with other users
    @GetMapping("/music-matches")
    public String findMusicMatches(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            // This will find users with similar music taste
            var matches = spotifyService.findMusicMatches(currentUser);
            model.addAttribute("matches", matches);
        }

        return "music-matches";
    }

    // Control Spotify playback
    @PostMapping("/spotify/play")
    public String playMusic(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().getSpotifyAccessToken() != null) {
            spotifyService.startPlayback(userOpt.get().getSpotifyAccessToken());
        }
        return "redirect:/spotify-success";
    }
}