package com.example.soundscape.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // No duplicate usernames
    private String username;

    @Column(unique = true, nullable = false) // No duplicate emails
    private String email;

    @Column(nullable = false) // Password required
    private String password;

    private Instant createdAt;

    public User() {}

    // Constructor for registration
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = Instant.now();
    }

    // Getters
    public Long getId() {return id;}
    public String getUsername() {return username;}
    public String getEmail() {return email;}
    public String getPassword() {return password;}
    public Instant getCreatedAt() {return createdAt;}
}
