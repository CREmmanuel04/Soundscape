package com.example.soundscape.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private String author;
    private Instant createdAt;
    private int likeCount;
    private boolean edited = false;
    private Instant editedAt;

    @ElementCollection
    private Set<String> likedBy = new HashSet<>();

    public Post() {}

    // Constructor for creating new posts
    public Post(String content, String author) {
        this.content = content;
        this.author = author;
        this.createdAt = Instant.now();
        this.likeCount = 0;
    }

    // Getters only (immutable once created)
    public Long getId() {return id;}
    public String getContent() {return content;}
    public String getAuthor() {return author;}
    public Instant getCreatedAt() {return createdAt;}
    public int getLikeCount() {return likeCount;}
    public Set<String> getLikedBy() {return likedBy;}
    public boolean isEdited() {return edited;}
    public Instant getEditedAt() {return editedAt;}

    // Setters (for likes only, posts are immutable)
    public void setLikeCount(int likeCount) {this.likeCount = likeCount;}

    // Methods for likes
    public void addLike(String username) {
        likedBy.add(username);
    }

    public void removeLike(String username) {
        likedBy.remove(username);
    }

    public boolean isLikedBy(String username) {
        return likedBy.contains(username);
    }

    // Method for editing
    public void updateContent(String newContent) {
        this.content = newContent;
        this.edited = true;
        this.editedAt = Instant.now();
    }
}
