package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import com.example.soundscape.services.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class FollowController {
    
    private final FollowService followService;
    private final UserRepository userRepository;
    
    public FollowController(FollowService followService, UserRepository userRepository) {
        this.followService = followService;
        this.userRepository = userRepository;
    }
    
    /**
     * Follow a user (AJAX endpoint)
     */
    @PostMapping("/api/follow/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> followUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.ok(response);
        }
        
        Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
        if (currentUserOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }
        
        Long currentUserId = currentUserOpt.get().getId();
        boolean success = followService.followUser(currentUserId, userId);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Successfully followed user");
            response.put("followerCount", followService.getFollowerCount(userId));
            response.put("isFollowing", true);
        } else {
            response.put("success", false);
            response.put("message", "Failed to follow user");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unfollow a user (AJAX endpoint)
     */
    @DeleteMapping("/api/follow/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unfollowUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.ok(response);
        }
        
        Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
        if (currentUserOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }
        
        Long currentUserId = currentUserOpt.get().getId();
        boolean success = followService.unfollowUser(currentUserId, userId);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Successfully unfollowed user");
            response.put("followerCount", followService.getFollowerCount(userId));
            response.put("isFollowing", false);
        } else {
            response.put("success", false);
            response.put("message", "Failed to unfollow user");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get followers list for a user
     */
    @GetMapping("/api/users/{userId}/followers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFollowers(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        Long currentUserId = null;
        if (userDetails != null) {
            Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
            if (currentUserOpt.isPresent()) {
                currentUserId = currentUserOpt.get().getId();
            }
        }
        
        List<User> followers = followService.getFollowers(userId);
        
        // Populate follow stats for each follower
        for (User follower : followers) {
            followService.populateFollowStats(follower, currentUserId);
        }
        
        response.put("success", true);
        response.put("followers", followers);
        response.put("count", followers.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get following list for a user
     */
    @GetMapping("/api/users/{userId}/following")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFollowing(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        Long currentUserId = null;
        if (userDetails != null) {
            Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
            if (currentUserOpt.isPresent()) {
                currentUserId = currentUserOpt.get().getId();
            }
        }
        
        List<User> following = followService.getFollowing(userId);
        
        // Populate follow stats for each user being followed
        for (User user : following) {
            followService.populateFollowStats(user, currentUserId);
        }
        
        response.put("success", true);
        response.put("following", following);
        response.put("count", following.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * View user profile by ID
     */
    @GetMapping("/user/{userId}")
    public String viewUserProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/feed?error=User+not+found";
        }
        
        User user = userOpt.get();
        
        // Get current user for follow status
        Long currentUserId = null;
        if (userDetails != null) {
            Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
            if (currentUserOpt.isPresent()) {
                currentUserId = currentUserOpt.get().getId();
            }
        }
        
        // Populate follow statistics
        user = followService.populateFollowStats(user, currentUserId);
        
        model.addAttribute("user", user);
        model.addAttribute("isOwnProfile", currentUserId != null && currentUserId.equals(userId));
        
        return "profile";
    }
    
    /**
     * Check follow status (AJAX endpoint)
     */
    @GetMapping("/api/follow/status/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFollowStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.ok(response);
        }
        
        Optional<User> currentUserOpt = userRepository.findByUsername(userDetails.getUsername());
        if (currentUserOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }
        
        Long currentUserId = currentUserOpt.get().getId();
        
        response.put("success", true);
        response.put("isFollowing", followService.isFollowing(currentUserId, userId));
        response.put("followerCount", followService.getFollowerCount(userId));
        response.put("followingCount", followService.getFollowingCount(userId));
        response.put("areMutual", followService.areMutualFollowers(currentUserId, userId));
        
        return ResponseEntity.ok(response);
    }
}