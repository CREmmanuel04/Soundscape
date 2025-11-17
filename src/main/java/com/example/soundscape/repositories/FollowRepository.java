package com.example.soundscape.repositories;

import com.example.soundscape.models.Follow;
import com.example.soundscape.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    // Check if user1 follows user2
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    // Check if follow relationship exists
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    // Get all users that a user is following
    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user")
    List<User> findFollowingByUser(@Param("user") User user);
    
    // Get all users that follow a user (followers)
    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user")
    List<User> findFollowersByUser(@Param("user") User user);
    
    // Count how many people a user is following
    long countByFollower(User follower);
    
    // Count how many followers a user has
    long countByFollowing(User following);
    
    // Delete follow relationship
    void deleteByFollowerAndFollowing(User follower, User following);
    
    // Find mutual follows (both users follow each other)
    @Query("SELECT f1.following FROM Follow f1 WHERE f1.follower = :user " +
           "AND EXISTS (SELECT f2 FROM Follow f2 WHERE f2.follower = f1.following AND f2.following = :user)")
    List<User> findMutualFollows(@Param("user") User user);
    
    // Check if two users follow each other mutually
    @Query("SELECT COUNT(f) = 2 FROM Follow f WHERE " +
           "(f.follower = :user1 AND f.following = :user2) OR " +
           "(f.follower = :user2 AND f.following = :user1)")
    boolean areMutualFollowers(@Param("user1") User user1, @Param("user2") User user2);
}