package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";  // Will look for login.html
    }

    // Show registration form
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";  // Will look for register.html
    }

    // Process registration form
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password) {

        // Basic validation
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return "redirect:/register?error=empty_fields";
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            return "redirect:/register?error=username_taken";
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return "redirect:/register?error=email_taken";
        }

        // Create and save new user
        User newUser = new User(username, email, passwordEncoder.encode(password));
        userRepository.save(newUser);

        return "redirect:/?registered=true";  // Redirect to home page
    }
}