package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.services.UserService;
import com.example.soundscape.services.FollowService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;
    private final FollowService followService;

    public ProfileController(UserService userService, FollowService followService) {
        this.userService = userService;
        this.followService = followService;
    }

    // Available profile icons (emojis)
    private final List<String> AVAILABLE_ICONS = Arrays.asList(
        "😀", "😎", "🤩", "😊", "🥳", "😇", "🤓", "🧠", "🎵", "🎸", 
        "🎤", "🎧", "🎹", "🥁", "🎺", "🎪", "🌟", "⭐", "🔥", "💎",
        "🌈", "🦄", "🐱", "🐶", "🦋", "🌸", "🌺", "🍀", "⚡", "🌙"
    );

    // Available banner colors
    private final List<String> AVAILABLE_COLORS = Arrays.asList(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", 
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
        "#F8C471", "#82E0AA", "#F1948A", "#85C1E9", "#D7BDE2"
    );

    @GetMapping("/profile")
    public String showProfile(@RequestParam(required = false) String username, Model model, Authentication authentication) {
        String targetUsername = (username != null) ? username : authentication.getName();
        User user = userService.findByUsername(targetUsername);
        
        if (user == null) {
            return "redirect:/";
        }
        
        // Get current user ID for follow status
        User currentUser = userService.findByUsername(authentication.getName());
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        
        // Populate follow statistics
        user = followService.populateFollowStats(user, currentUserId);
        
        model.addAttribute("user", user);
        model.addAttribute("isOwnProfile", targetUsername.equals(authentication.getName()));
        
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        
        if (user == null) {
            return "redirect:/";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("availableIcons", AVAILABLE_ICONS);
        model.addAttribute("availableColors", AVAILABLE_COLORS);
        
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@RequestParam String profileIcon,
                               @RequestParam String bannerColor,
                               @RequestParam String bio,
                               @RequestParam(required = false) String favoriteSongId,
                               @RequestParam(required = false) String favoriteSongName,
                               @RequestParam(required = false) String favoriteSongArtist,
                               @RequestParam(required = false) String favoriteSongImage,
                               @RequestParam(required = false) String favoriteSongUrl,
                               Authentication authentication) {
        
        User user = userService.findByUsername(authentication.getName());
        
        if (user != null) {
            // Validate profile icon
            if (AVAILABLE_ICONS.contains(profileIcon)) {
                user.setProfileIcon(profileIcon);
            }
            
            // Validate banner color
            if (AVAILABLE_COLORS.contains(bannerColor)) {
                user.setBannerColor(bannerColor);
            }
            
            // Set bio (limit to 500 characters)
            if (bio != null && bio.length() <= 500) {
                user.setBio(bio.trim());
            }
            
            // Set favorite song if provided
            if (favoriteSongId != null && !favoriteSongId.trim().isEmpty()) {
                user.setFavoriteSongId(favoriteSongId.trim());
                user.setFavoriteSongName(favoriteSongName != null ? favoriteSongName.trim() : "");
                user.setFavoriteSongArtist(favoriteSongArtist != null ? favoriteSongArtist.trim() : "");
                user.setFavoriteSongImage(favoriteSongImage != null ? favoriteSongImage.trim() : "");
                user.setFavoriteSongUrl(favoriteSongUrl != null ? favoriteSongUrl.trim() : "");
            }
            
            userService.save(user);
        }
        
        return "redirect:/profile";
    }
}