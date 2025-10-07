package com.example.soundscape.services;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SpotifyService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public SpotifyService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    // Get user's top artists from Spotify API
    public String getUserTopArtists(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Spotify API endpoint for user's top artists
            String url = "https://api.spotify.com/v1/me/top/artists?limit=10";

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody(); // Returns JSON with top artists
            }
        } catch (Exception e) {
            System.out.println("Error fetching top artists: " + e.getMessage());
        }
        return "Unable to fetch top artists";
    }

    // Find users with similar music taste
    public List<User> findMusicMatches(User currentUser) {
        List<User> allUsers = userRepository.findAll();
        List<User> matches = new ArrayList<>();

        // For now, just return all other users (we'll implement actual matching later)
        for (User user : allUsers) {
            if (!user.getId().equals(currentUser.getId()) &&
                    user.getSpotifyAccessToken() != null) {
                matches.add(user);
            }
        }

        return matches;
    }

    // Start playback on user's Spotify
    public void startPlayback(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Simple play command - you can customize this
            String playBody = "{\"context_uri\":\"spotify:album:5zT1JLIj9E57p3e1rFm9Uq\"}"; // Example album

            HttpEntity<String> entity = new HttpEntity<>(playBody, headers);

            String url = "https://api.spotify.com/v1/me/player/play";

            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        } catch (Exception e) {
            System.out.println("Error starting playback: " + e.getMessage());
        }
    }

    // Get currently playing track
    public String getCurrentlyPlaying(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://api.spotify.com/v1/me/player/currently-playing";

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.out.println("Error getting currently playing: " + e.getMessage());
        }
        return "No track currently playing";
    }
}