package com.example.soundscape.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String author;
    private Instant createdAt;

    public Post() {}

    // Constructor for creating new posts
    public Post(String content, String author) {
        this.content = content;
        this.author = author;
        this.createdAt = Instant.now();
    }

    // Getters only (immutable once created)
    public Long getId() {return id;}
    public String getContent() {return content;}
    public String getAuthor() {return author;}
    public Instant getCreatedAt() {return createdAt;}
}
