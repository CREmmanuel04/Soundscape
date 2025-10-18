package com.example.soundscape.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Instant createdAt;

    // NEW: Spotify integration fields
    @Column(name = "spotify_access_token", length = 2000)
    private String spotifyAccessToken;

    @Column(name = "spotify_refresh_token", length = 2000)
    private String spotifyRefreshToken;

    @Column(name = "spotify_user_id")
    private String spotifyUserId;

    @Column(name = "spotify_display_name")
    private String spotifyDisplayName;

    @Column(name = "top_artists", length = 1000) // Store as JSON string
    private String topArtists;

    @Column(name = "spotify_connected_at")
    private Instant spotifyConnectedAt;

    public User() {}

    // Constructor for registration (without Spotify)
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Instant getCreatedAt() { return createdAt; }

    // NEW: Spotify getters
    public String getSpotifyAccessToken() { return spotifyAccessToken; }
    public String getSpotifyRefreshToken() { return spotifyRefreshToken; }
    public String getSpotifyUserId() { return spotifyUserId; }
    public String getSpotifyDisplayName() { return spotifyDisplayName; }
    public String getTopArtists() { return topArtists; }
    public Instant getSpotifyConnectedAt() { return spotifyConnectedAt; }

    // NEW: Spotify setters
    public void setSpotifyAccessToken(String spotifyAccessToken) {
        this.spotifyAccessToken = spotifyAccessToken;
    }

    public void setSpotifyRefreshToken(String spotifyRefreshToken) {
        this.spotifyRefreshToken = spotifyRefreshToken;
    }

    public void setSpotifyUserId(String spotifyUserId) {
        this.spotifyUserId = spotifyUserId;
    }

    public void setSpotifyDisplayName(String spotifyDisplayName) {
        this.spotifyDisplayName = spotifyDisplayName;
    }

    public void setTopArtists(String topArtists) {
        this.topArtists = topArtists;
    }

    public void setSpotifyConnectedAt(Instant spotifyConnectedAt) {
        this.spotifyConnectedAt = spotifyConnectedAt;
    }

    // Helper method to check if Spotify is connected
    public boolean isSpotifyConnected() {
        return spotifyAccessToken != null && !spotifyAccessToken.isEmpty();
    }
}