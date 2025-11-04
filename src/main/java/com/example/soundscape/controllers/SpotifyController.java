package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import com.example.soundscape.services.SpotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    // Enhanced Now Playing with playlists and advanced controls
    @GetMapping("/now-playing-enhanced")
    public String nowPlayingEnhanced(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean hasSpotifyToken = user.isSpotifyConnected();
            model.addAttribute("connected", hasSpotifyToken);

            if (hasSpotifyToken) {
                // Get Spotify profile
                Map<String, String> spotifyProfile = spotifyService.getUserProfile(user.getSpotifyAccessToken());
                model.addAttribute("spotifyProfile", spotifyProfile);

                // Get currently playing track
                Map<String, String> currentlyPlaying = spotifyService.getCurrentlyPlaying(user.getSpotifyAccessToken());
                model.addAttribute("trackInfo", currentlyPlaying);

                // Get user's playlists
                Map<String, Object> playlists = spotifyService.getUserPlaylists(user.getSpotifyAccessToken(), 50);
                model.addAttribute("playlists", playlists.get("playlists"));

                // Get available devices
                Map<String, Object> devices = spotifyService.getAvailableDevices(user.getSpotifyAccessToken());
                model.addAttribute("devices", devices.get("devices"));

                // Pass user to frontend
                model.addAttribute("user", user);
            }
        } else {
            model.addAttribute("connected", false);
        }

        return "now-playing-enhanced";
    }

    // API endpoint for getting playlists (AJAX)
    @GetMapping("/api/spotify/playlists")
    @ResponseBody
    public Map<String, Object> getPlaylists(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            return spotifyService.getUserPlaylists(user.getSpotifyAccessToken(), 50);
        }

        return Map.of("error", "Spotify not connected");
    }

    // API endpoint for getting playlist tracks (AJAX)
    @GetMapping("/api/spotify/playlist/{playlistId}/tracks")
    @ResponseBody
    public Map<String, Object> getPlaylistTracks(
            @PathVariable String playlistId,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            return spotifyService.getPlaylistTracks(user.getSpotifyAccessToken(), playlistId, limit);
        }

        return Map.of("error", "Spotify not connected");
    }

    // API endpoint for playback control
    @PostMapping("/api/spotify/control/{action}")
    @ResponseBody
    public Map<String, Object> controlPlayback(
            @PathVariable String action,
            @RequestParam(required = false) String playlistId,
            @RequestParam(required = false) Integer volume,
            @RequestParam(required = false) String deviceId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent() || !userOpt.get().isSpotifyConnected()) {
            return Map.of("error", "Spotify not connected");
        }

        User user = userOpt.get();
        String accessToken = user.getSpotifyAccessToken();

        try {
            switch (action.toLowerCase()) {
                case "play":
                    if (playlistId != null && !playlistId.isEmpty()) {
                        spotifyService.playPlaylist(accessToken, playlistId);
                    } else {
                        spotifyService.resumePlayback(accessToken);
                    }
                    break;
                case "pause":
                    spotifyService.pausePlayback(accessToken);
                    break;
                case "next":
                    spotifyService.skipToNext(accessToken);
                    break;
                case "previous":
                    spotifyService.skipToPrevious(accessToken);
                    break;
                case "volume":
                    if (volume != null && volume >= 0 && volume <= 100) {
                        spotifyService.setVolume(accessToken, volume);
                    } else {
                        return Map.of("error", "Invalid volume level");
                    }
                    break;
                case "transfer":
                    if (deviceId != null && !deviceId.isEmpty()) {
                        spotifyService.transferPlayback(accessToken, deviceId, true);
                    } else {
                        return Map.of("error", "Device ID required");
                    }
                    break;
                default:
                    return Map.of("error", "Unknown action: " + action);
            }

            return Map.of("success", true, "action", action);

        } catch (Exception e) {
            return Map.of("error", "Failed to execute action: " + e.getMessage());
        }
    }

    // API endpoint for current playback status
    @GetMapping("/api/spotify/status")
    @ResponseBody
    public Map<String, Object> getPlaybackStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            Map<String, String> currentlyPlaying = spotifyService.getCurrentlyPlaying(user.getSpotifyAccessToken());
            return new HashMap<>(currentlyPlaying);
        }

        return Map.of("error", "Spotify not connected");
    }

    // API endpoint for devices
    @GetMapping("/api/spotify/devices")
    @ResponseBody
    public Map<String, Object> getDevices(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isPresent() && userOpt.get().isSpotifyConnected()) {
            User user = userOpt.get();
            return spotifyService.getAvailableDevices(user.getSpotifyAccessToken());
        }

        return Map.of("error", "Spotify not connected");
    }

    // API endpoint for playing a specific playlist
    @PostMapping("/api/spotify/play-playlist/{playlistId}")
    @ResponseBody
    public Map<String, Object> playPlaylist(
            @PathVariable String playlistId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent() || !userOpt.get().isSpotifyConnected()) {
            return Map.of("error", "Spotify not connected");
        }

        try {
            User user = userOpt.get();
            spotifyService.playPlaylist(user.getSpotifyAccessToken(), playlistId);
            return Map.of("success", true, "playlistId", playlistId);
        } catch (Exception e) {
            return Map.of("error", "Failed to play playlist: " + e.getMessage());
        }
    }

    // API endpoint for playing a specific track
    @PostMapping("/api/spotify/play-track/{trackId}")
    @ResponseBody
    public Map<String, Object> playTrack(
            @PathVariable String trackId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent() || !userOpt.get().isSpotifyConnected()) {
            return Map.of("error", "Spotify not connected");
        }

        try {
            User user = userOpt.get();
            spotifyService.playTrack(user.getSpotifyAccessToken(), trackId);
            return Map.of("success", true, "trackId", trackId);
        } catch (Exception e) {
            return Map.of("error", "Failed to play track: " + e.getMessage());
        }
    }

    // API endpoint for searching tracks (for favorite song selection)
    @GetMapping("/api/spotify/search")
    @ResponseBody
    public Map<String, Object> searchTracks(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return Map.of("error", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent() || !userOpt.get().isSpotifyConnected()) {
            return Map.of("error", "Spotify not connected");
        }

        try {
            User user = userOpt.get();
            var tracks = spotifyService.searchTracks(user.getSpotifyAccessToken(), query);
            return Map.of("tracks", tracks);
        } catch (Exception e) {
            return Map.of("error", "Failed to search tracks: " + e.getMessage());
        }
    }

    // API endpoint for playing a specific track (alternative route for compatibility)
    @PostMapping("/api/spotify/play/{trackId}")
    @ResponseBody
    public Map<String, Object> playTrackAlternative(
            @PathVariable String trackId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return Map.of("status", "error", "message", "Not authenticated");
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (!userOpt.isPresent() || !userOpt.get().isSpotifyConnected()) {
            return Map.of("status", "error", "message", "Spotify not connected");
        }

        try {
            User user = userOpt.get();
            spotifyService.playTrack(user.getSpotifyAccessToken(), trackId);
            return Map.of("status", "success", "trackId", trackId);
        } catch (Exception e) {
            return Map.of("status", "error", "message", "Failed to play track: " + e.getMessage());
        }
    }
}