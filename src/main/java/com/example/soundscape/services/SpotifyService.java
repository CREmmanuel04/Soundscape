package com.example.soundscape.services;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Get Spotify user profile to verify connection
    public Map<String, String> getUserProfile(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://api.spotify.com/v1/me";

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                Map<String, String> profile = new HashMap<>();
                profile.put("displayName", root.get("display_name").asText());
                profile.put("email", root.has("email") ? root.get("email").asText() : "");
                profile.put("profileImage", root.get("images").get(0).get("url").asText());

                return profile;
            }
        } catch (Exception e) {
            System.out.println("Error fetching user profile: " + e.getMessage());
        }

        Map<String, String> fallback = new HashMap<>();
        fallback.put("displayName", "Unknown");
        return fallback;
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

    // Play a specific track by ID
    public void playTrack(String accessToken, String trackId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Play specific track using Spotify track URI
            String playBody = "{\"uris\":[\"spotify:track:" + trackId + "\"]}";

            HttpEntity<String> entity = new HttpEntity<>(playBody, headers);

            String url = "https://api.spotify.com/v1/me/player/play";

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Successfully started playing track: " + trackId);
            } else {
                System.out.println("Failed to play track. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("Error playing track " + trackId + ": " + e.getMessage());
            throw new RuntimeException("Failed to play track: " + e.getMessage());
        }
    }

    // Search for tracks on Spotify
    public List<Map<String, String>> searchTracks(String accessToken, String query) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // URL encode the query
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=10";

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode tracks = root.get("tracks").get("items");

                List<Map<String, String>> trackList = new ArrayList<>();
                
                for (JsonNode track : tracks) {
                    Map<String, String> trackInfo = new HashMap<>();
                    trackInfo.put("id", track.get("id").asText());
                    trackInfo.put("name", track.get("name").asText());
                    trackInfo.put("artist", track.get("artists").get(0).get("name").asText());
                    trackInfo.put("album", track.get("album").get("name").asText());
                    
                    // Get album image if available
                    if (track.get("album").has("images") && track.get("album").get("images").size() > 0) {
                        trackInfo.put("image", track.get("album").get("images").get(0).get("url").asText());
                    } else {
                        trackInfo.put("image", "");
                    }
                    
                    trackInfo.put("spotifyUrl", track.get("external_urls").get("spotify").asText());
                    trackList.add(trackInfo);
                }
                
                return trackList;
            }
        } catch (Exception e) {
            System.out.println("Error searching tracks: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    // Get track details by Spotify ID
    public Map<String, String> getTrackById(String accessToken, String trackId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://api.spotify.com/v1/tracks/" + trackId;

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode track = mapper.readTree(response.getBody());

                Map<String, String> trackInfo = new HashMap<>();
                trackInfo.put("id", track.get("id").asText());
                trackInfo.put("name", track.get("name").asText());
                trackInfo.put("artist", track.get("artists").get(0).get("name").asText());
                trackInfo.put("album", track.get("album").get("name").asText());
                
                if (track.get("album").has("images") && track.get("album").get("images").size() > 0) {
                    trackInfo.put("image", track.get("album").get("images").get(0).get("url").asText());
                } else {
                    trackInfo.put("image", "");
                }
                
                trackInfo.put("spotifyUrl", track.get("external_urls").get("spotify").asText());
                return trackInfo;
            }
        } catch (Exception e) {
            System.out.println("Error getting track details: " + e.getMessage());
        }

        return new HashMap<>();
    }

    // Get currently playing track
    public Map<String, String> getCurrentlyPlaying(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://api.spotify.com/v1/me/player/currently-playing";

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                Map<String, String> trackInfo = new HashMap<>();

                if (root.has("is_playing") && root.get("is_playing").asBoolean()) {
                    JsonNode item = root.get("item");
                    trackInfo.put("trackName", item.get("name").asText());
                    trackInfo.put("artistName", item.get("artists").get(0).get("name").asText());
                    trackInfo.put("albumName", item.get("album").get("name").asText());
                    trackInfo.put("albumImage", item.get("album").get("images").get(0).get("url").asText());
                    trackInfo.put("isPlaying", "true");
                } else {
                    trackInfo.put("isPlaying", "false");
                    trackInfo.put("trackName", "No track currently playing");
                }

                return trackInfo;
            }
        } catch (Exception e) {
            System.out.println("Error getting currently playing: " + e.getMessage());
        }

        Map<String, String> fallback = new HashMap<>();
        fallback.put("isPlaying", "false");
        fallback.put("trackName", "Unable to fetch track info");
        return fallback;
    }
}