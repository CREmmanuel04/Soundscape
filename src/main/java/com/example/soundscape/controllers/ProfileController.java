package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserService userService;
    private final Path uploadsDir = Paths.get("uploads").toAbsolutePath();

    public ProfileController(UserService userService) {
        this.userService = userService;

        try {
            Files.createDirectories(uploadsDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create uploads directory", e);
        }
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "profile-edit";
    }

    @GetMapping("/profile/{username}")
    public String viewProfile(@org.springframework.web.bind.annotation.PathVariable String username, Model model) {
        try {
            User user = userService.findByUsername(username);
            model.addAttribute("user", user);
            return "profile";
        } catch (RuntimeException e) {
            return "redirect:/?userNotFound";
        }
    }

    @PostMapping("/profile/edit")
    public String saveProfile(Authentication authentication,
                              @RequestParam(name = "profilePicture", required = false) MultipartFile profilePicture,
                              @RequestParam(name = "banner", required = false) MultipartFile banner,
                              @RequestParam(name = "bio", required = false) String bio) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(authentication.getName());

        // Save bio
        if (bio != null) {
            user.setBio(bio.trim());
        }

        try {
            // Validate and store profile picture
            if (profilePicture != null && !profilePicture.isEmpty()) {
                if (!isImage(profilePicture)) return "redirect:/profile/edit?error=invalid_profile_image";
                String filename = storeFile(profilePicture);
                user.setProfilePicture("/uploads/" + filename);
            }

            // Validate and store banner
            if (banner != null && !banner.isEmpty()) {
                if (!isImage(banner)) return "redirect:/profile/edit?error=invalid_banner_image";
                String filename = storeFile(banner);
                user.setBanner("/uploads/" + filename);
            }

            userService.save(user);
            return "redirect:/profile/edit?success";
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/profile/edit?error=io_exception";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/profile/edit?error=unknown";
        }
    }

    private String storeFile(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx >= 0) ext = original.substring(idx);
        String filename = UUID.randomUUID().toString() + ext;
        Path target = uploadsDir.resolve(filename);
        // Copy and replace existing file if any
        Files.copy(file.getInputStream(), target);
        // Ensure file is readable
        File f = target.toFile();
        f.setReadable(true, false);
        return filename;
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;
        return contentType.startsWith("image/");
    }
}
