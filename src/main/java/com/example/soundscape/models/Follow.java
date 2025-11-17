package com.example.soundscape.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "follows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
public class Follow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;
    
    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;
    
    // Constructors
    public Follow() {
        this.followedAt = Instant.now();
    }
    
    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
        this.followedAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getFollower() {
        return follower;
    }
    
    public void setFollower(User follower) {
        this.follower = follower;
    }
    
    public User getFollowing() {
        return following;
    }
    
    public void setFollowing(User following) {
        this.following = following;
    }
    
    public Instant getFollowedAt() {
        return followedAt;
    }
    
    public void setFollowedAt(Instant followedAt) {
        this.followedAt = followedAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Follow follow = (Follow) obj;
        return follower != null && following != null && 
               follower.getId().equals(follow.follower.getId()) &&
               following.getId().equals(follow.following.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}