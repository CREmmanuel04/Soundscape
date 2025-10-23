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

    // Get user's top tracks with detailed information
    public Map<String, Object> getUserTopTracks(String accessToken, String timeRange, int limit) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = String.format("https://api.spotify.com/v1/me/top/tracks?limit=%d&time_range=%s", 
                                       limit, timeRange);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> tracks = new ArrayList<>();
                
                for (JsonNode item : root.get("items")) {
                    Map<String, Object> track = new HashMap<>();
                    track.put("name", item.get("name").asText());
                    track.put("popularity", item.get("popularity").asInt());
                    track.put("duration_ms", item.get("duration_ms").asLong());
                    track.put("explicit", item.get("explicit").asBoolean());
                    
                    // Artist information
                    List<String> artists = new ArrayList<>();
                    for (JsonNode artist : item.get("artists")) {
                        artists.add(artist.get("name").asText());
                    }
                    track.put("artists", artists);
                    
                    // Album information
                    JsonNode album = item.get("album");
                    track.put("album_name", album.get("name").asText());
                    track.put("release_date", album.get("release_date").asText());
                    
                    // Image
                    if (album.has("images") && album.get("images").size() > 0) {
                        track.put("image_url", album.get("images").get(0).get("url").asText());
                    }
                    
                    tracks.add(track);
                }
                
                result.put("tracks", tracks);
                result.put("total", root.get("total").asInt());
                return result;
            }
        } catch (Exception e) {
            System.out.println("Error fetching top tracks: " + e.getMessage());
        }
        
        return new HashMap<>();
    }

    // Get user's top artists with detailed information and genres
    public Map<String, Object> getUserTopArtistsDetailed(String accessToken, String timeRange, int limit) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = String.format("https://api.spotify.com/v1/me/top/artists?limit=%d&time_range=%s", 
                                       limit, timeRange);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> artists = new ArrayList<>();
                Map<String, Integer> genreCount = new HashMap<>();
                
                for (JsonNode item : root.get("items")) {
                    Map<String, Object> artist = new HashMap<>();
                    artist.put("name", item.get("name").asText());
                    artist.put("popularity", item.get("popularity").asInt());
                    artist.put("followers", item.get("followers").get("total").asLong());
                    
                    // Genres
                    List<String> genres = new ArrayList<>();
                    for (JsonNode genre : item.get("genres")) {
                        String genreName = genre.asText();
                        genres.add(genreName);
                        genreCount.put(genreName, genreCount.getOrDefault(genreName, 0) + 1);
                    }
                    artist.put("genres", genres);
                    
                    // Image
                    if (item.has("images") && item.get("images").size() > 0) {
                        artist.put("image_url", item.get("images").get(0).get("url").asText());
                    }
                    
                    artists.add(artist);
                }
                
                result.put("artists", artists);
                result.put("genre_analysis", genreCount);
                result.put("total", root.get("total").asInt());
                return result;
            }
        } catch (Exception e) {
            System.out.println("Error fetching top artists: " + e.getMessage());
        }
        
        return new HashMap<>();
    }

    // Get comprehensive user music analysis
    public Map<String, Object> getUserMusicAnalysis(String accessToken) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Get data for different time ranges
        Map<String, Object> shortTerm = getUserTopArtistsDetailed(accessToken, "short_term", 20);
        Map<String, Object> mediumTerm = getUserTopArtistsDetailed(accessToken, "medium_term", 20);
        Map<String, Object> longTerm = getUserTopArtistsDetailed(accessToken, "long_term", 20);
        
        Map<String, Object> shortTermTracks = getUserTopTracks(accessToken, "short_term", 20);
        Map<String, Object> mediumTermTracks = getUserTopTracks(accessToken, "medium_term", 20);
        Map<String, Object> longTermTracks = getUserTopTracks(accessToken, "long_term", 20);
        
        // Combine genre analysis
        Map<String, Integer> combinedGenres = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> shortGenres = (Map<String, Integer>) shortTerm.getOrDefault("genre_analysis", new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, Integer> mediumGenres = (Map<String, Integer>) mediumTerm.getOrDefault("genre_analysis", new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, Integer> longGenres = (Map<String, Integer>) longTerm.getOrDefault("genre_analysis", new HashMap<>());
        
        // Weight recent genres more heavily
        for (Map.Entry<String, Integer> entry : shortGenres.entrySet()) {
            combinedGenres.put(entry.getKey(), combinedGenres.getOrDefault(entry.getKey(), 0) + entry.getValue() * 3);
        }
        for (Map.Entry<String, Integer> entry : mediumGenres.entrySet()) {
            combinedGenres.put(entry.getKey(), combinedGenres.getOrDefault(entry.getKey(), 0) + entry.getValue() * 2);
        }
        for (Map.Entry<String, Integer> entry : longGenres.entrySet()) {
            combinedGenres.put(entry.getKey(), combinedGenres.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
        
        // Sort genres by frequency
        List<Map.Entry<String, Integer>> sortedGenres = new ArrayList<>(combinedGenres.entrySet());
        sortedGenres.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        
        // Convert to array format for frontend
        List<Object[]> topGenresArray = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedGenres.subList(0, Math.min(10, sortedGenres.size()))) {
            topGenresArray.add(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        // Build final analysis
        analysis.put("top_genres", topGenresArray);
        analysis.put("short_term_artists", shortTerm.get("artists"));
        analysis.put("medium_term_artists", mediumTerm.get("artists"));
        analysis.put("long_term_artists", longTerm.get("artists"));
        analysis.put("short_term_tracks", shortTermTracks.get("tracks"));
        analysis.put("medium_term_tracks", mediumTermTracks.get("tracks"));
        analysis.put("long_term_tracks", longTermTracks.get("tracks"));
        
        return analysis;
    }

    // Get recently played tracks
    public Map<String, Object> getRecentlyPlayed(String accessToken, int limit) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = String.format("https://api.spotify.com/v1/me/player/recently-played?limit=%d", limit);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> tracks = new ArrayList<>();
                
                for (JsonNode item : root.get("items")) {
                    JsonNode track = item.get("track");
                    Map<String, Object> trackInfo = new HashMap<>();
                    
                    trackInfo.put("name", track.get("name").asText());
                    trackInfo.put("played_at", item.get("played_at").asText());
                    
                    // Artists
                    List<String> artists = new ArrayList<>();
                    for (JsonNode artist : track.get("artists")) {
                        artists.add(artist.get("name").asText());
                    }
                    trackInfo.put("artists", artists);
                    
                    // Album
                    JsonNode album = track.get("album");
                    trackInfo.put("album_name", album.get("name").asText());
                    
                    if (album.has("images") && album.get("images").size() > 0) {
                        trackInfo.put("image_url", album.get("images").get(0).get("url").asText());
                    }
                    
                    tracks.add(trackInfo);
                }
                
                result.put("tracks", tracks);
                return result;
            }
        } catch (Exception e) {
            System.out.println("Error fetching recently played: " + e.getMessage());
        }
        
        return new HashMap<>();
    }
}