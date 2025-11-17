package com.example.soundscape.services;

import com.example.soundscape.models.Follow;
import com.example.soundscape.models.User;
import com.example.soundscape.repositories.FollowRepository;
import com.example.soundscape.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FollowService {
    
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    
    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Follow a user
     */
    public boolean followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return false; // Cannot follow yourself
        }
        
        Optional<User> followerOpt = userRepository.findById(followerId);
        Optional<User> followingOpt = userRepository.findById(followingId);
        
        if (followerOpt.isEmpty() || followingOpt.isEmpty()) {
            return false; // Users don't exist
        }
        
        User follower = followerOpt.get();
        User following = followingOpt.get();
        
        // Check if already following
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return false; // Already following
        }
        
        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
        return true;
    }
    
    /**
     * Unfollow a user
     */
    public boolean unfollowUser(Long followerId, Long followingId) {
        Optional<User> followerOpt = userRepository.findById(followerId);
        Optional<User> followingOpt = userRepository.findById(followingId);
        
        if (followerOpt.isEmpty() || followingOpt.isEmpty()) {
            return false;
        }
        
        User follower = followerOpt.get();
        User following = followingOpt.get();
        
        followRepository.deleteByFollowerAndFollowing(follower, following);
        return true;
    }
    
    /**
     * Check if user1 follows user2
     */
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return false;
        }
        
        Optional<User> followerOpt = userRepository.findById(followerId);
        Optional<User> followingOpt = userRepository.findById(followingId);
        
        if (followerOpt.isEmpty() || followingOpt.isEmpty()) {
            return false;
        }
        
        return followRepository.existsByFollowerAndFollowing(followerOpt.get(), followingOpt.get());
    }
    
    /**
     * Get follower count for a user
     */
    public long getFollowerCount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return 0;
        }
        return followRepository.countByFollowing(userOpt.get());
    }
    
    /**
     * Get following count for a user
     */
    public long getFollowingCount(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return 0;
        }
        return followRepository.countByFollower(userOpt.get());
    }
    
    /**
     * Get list of followers for a user
     */
    public List<User> getFollowers(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        return followRepository.findFollowersByUser(userOpt.get());
    }
    
    /**
     * Get list of users that a user is following
     */
    public List<User> getFollowing(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        return followRepository.findFollowingByUser(userOpt.get());
    }
    
    /**
     * Check if two users follow each other mutually
     */
    public boolean areMutualFollowers(Long user1Id, Long user2Id) {
        Optional<User> user1Opt = userRepository.findById(user1Id);
        Optional<User> user2Opt = userRepository.findById(user2Id);
        
        if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
            return false;
        }
        
        return followRepository.areMutualFollowers(user1Opt.get(), user2Opt.get());
    }
    
    /**
     * Get mutual followers (users who both follow each other)
     */
    public List<User> getMutualFollowers(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        return followRepository.findMutualFollows(userOpt.get());
    }
    
    /**
     * Populate follow statistics for a user (for display purposes)
     */
    public User populateFollowStats(User user, Long currentUserId) {
        user.setFollowerCount(getFollowerCount(user.getId()));
        user.setFollowingCount(getFollowingCount(user.getId()));
        
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            user.setIsFollowing(isFollowing(currentUserId, user.getId()));
        } else {
            user.setIsFollowing(false); // Cannot follow yourself
        }
        
        return user;
    }
}