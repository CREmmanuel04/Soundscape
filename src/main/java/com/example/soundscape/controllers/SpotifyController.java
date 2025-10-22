package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import com.example.soundscape.services.SpotifyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class SpotifyController {

    private final UserRepository userRepository;
    private final SpotifyService spotifyService;

    public SpotifyController(UserRepository userRepository, SpotifyService spotifyService) {
        this.userRepository = userRepository;
        this.spotifyService = spotifyService;
    }

    // Simple now-playing page
    @GetMapping("/now-playing")
    public String nowPlaying(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean hasSpotifyToken = user.isSpotifyConnected();
            model.addAttribute("connected", hasSpotifyToken);

            if (hasSpotifyToken) {
                // Get parsed Spotify profile
                Map<String, String> spotifyProfile = spotifyService.getUserProfile(user.getSpotifyAccessToken());
                model.addAttribute("spotifyProfile", spotifyProfile);

                // Get parsed currently playing track
                Map<String, String> currentlyPlaying = spotifyService.getCurrentlyPlaying(user.getSpotifyAccessToken());
                model.addAttribute("trackInfo", currentlyPlaying);

                // Pass user to frontend (for now-playing.html)
                model.addAttribute("user", user);
            }
        } else {
            model.addAttribute("connected", false);
        }

        return "now-playing";
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
    
    // Direct link to Spotify OAuth (for testing)
    @GetMapping("/auth/spotify")
    public String authSpotify() {
        return "redirect:/oauth2/authorization/spotify";
    }
    
    // Test endpoint to simulate Spotify connection without OAuth (for development)
    @GetMapping("/test-spotify-connection")
    public String testSpotifyConnection(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Simulate a Spotify connection for testing
            user.setSpotifyUserId("test_user_" + user.getId());
            user.setSpotifyDisplayName("Test User");
            user.setSpotifyConnectedAt(java.time.Instant.now());
            
            userRepository.save(user);
            
            model.addAttribute("connected", true);
            model.addAttribute("message", "Test Spotify connection successful! (Development mode)");
            return "spotify-success";
        }
        
        return "redirect:/login";
    }

    // Add this method to your SpotifyController class
    @GetMapping("/login/oauth2/code/spotify")
    public String oauth2Callback(@RequestParam(required = false) String error,
                                 @RequestParam(required = false) String code,
                                 HttpServletRequest request) {
        System.out.println("=== OAUTH2 CALLBACK ===");
        System.out.println("Error: " + error);
        System.out.println("Code: " + (code != null ? "PRESENT" : "MISSING"));
        System.out.println("Request URL: " + request.getRequestURL());

        // Also check all parameters
        System.out.println("All parameters: " + request.getParameterMap());

        return "redirect:/";
    }

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

    // Search for tracks on Spotify (AJAX endpoint)
    @GetMapping("/api/spotify/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> searchTracks(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            List<Map<String, String>> tracks = spotifyService.searchTracks(user.getSpotifyAccessToken(), query);
            return ResponseEntity.ok(tracks);
        }

        return ResponseEntity.status(403).build(); // User not connected to Spotify
    }

    // Get track details by ID (AJAX endpoint)
    @GetMapping("/api/spotify/track")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getTrack(
            @RequestParam String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            Map<String, String> track = spotifyService.getTrackById(user.getSpotifyAccessToken(), id);
            return ResponseEntity.ok(track);
        }

        return ResponseEntity.status(403).build();
    }

    // Play a specific track (AJAX endpoint)
    @PostMapping("/api/spotify/play/{trackId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> playTrack(
            @PathVariable String trackId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            try {
                spotifyService.playTrack(user.getSpotifyAccessToken(), trackId);
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Track playback started");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Failed to start playback: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        return ResponseEntity.status(403).build();
    }
}