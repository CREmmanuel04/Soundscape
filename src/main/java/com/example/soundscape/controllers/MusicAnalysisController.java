package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import com.example.soundscape.services.SpotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Optional;

@Controller
public class MusicAnalysisController {

    private final UserRepository userRepository;
    private final SpotifyService spotifyService;

    public MusicAnalysisController(UserRepository userRepository, SpotifyService spotifyService) {
        this.userRepository = userRepository;
        this.spotifyService = spotifyService;
    }

    @GetMapping("/music-analysis")
    public String showMusicAnalysis(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            
            if (user.isSpotifyConnected()) {
                // Get comprehensive music analysis
                Map<String, Object> musicAnalysis = spotifyService.getUserMusicAnalysis(user.getSpotifyAccessToken());
                model.addAttribute("musicAnalysis", musicAnalysis);
                
                // Get recently played tracks
                Map<String, Object> recentlyPlayed = spotifyService.getRecentlyPlayed(user.getSpotifyAccessToken(), 20);
                model.addAttribute("recentlyPlayed", recentlyPlayed);
                
                model.addAttribute("connected", true);
            } else {
                model.addAttribute("connected", false);
            }
        }

        return "music-analysis";
    }

    // API endpoint for getting music analysis data (for AJAX requests)
    @GetMapping("/api/music-analysis")
    @ResponseBody
    public Map<String, Object> getMusicAnalysisData(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            try {
                Map<String, Object> analysis = spotifyService.getUserMusicAnalysis(user.getSpotifyAccessToken());
                System.out.println("Music analysis data: " + analysis);
                return analysis;
            } catch (Exception e) {
                System.err.println("Error fetching music analysis: " + e.getMessage());
                e.printStackTrace();
                return Map.of("error", "Failed to fetch music analysis: " + e.getMessage());
            }
        }

        return Map.of("error", "Spotify not connected");
    }

    // API endpoint for getting recently played tracks
    @GetMapping("/api/recently-played")
    @ResponseBody
    public Map<String, Object> getRecentlyPlayed(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            try {
                Map<String, Object> recentlyPlayed = spotifyService.getRecentlyPlayed(user.getSpotifyAccessToken(), 50);
                System.out.println("Recently played data: " + recentlyPlayed);
                return recentlyPlayed;
            } catch (Exception e) {
                System.err.println("Error fetching recently played: " + e.getMessage());
                e.printStackTrace();
                return Map.of("error", "Failed to fetch recently played tracks: " + e.getMessage());
            }
        }

        return Map.of("error", "Spotify not connected");
    }
}