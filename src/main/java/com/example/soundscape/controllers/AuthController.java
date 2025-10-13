package com.example.soundscape.controllers;

import com.example.soundscape.models.User;
import com.example.soundscape.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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

    // Show login form with OAuth debug
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "oauth2Error", required = false) String oauth2Error,
                                HttpServletRequest request) {

        if (error != null || oauth2Error != null) {
            System.out.println("=== OAUTH LOGIN ERROR DEBUG ===");
            System.out.println("Error parameter: " + error);
            System.out.println("OAuth2Error parameter: " + oauth2Error);

            // Check session for OAuth2 error
            Object sessionError = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            if (sessionError != null) {
                System.out.println("Session error attribute: " + sessionError);
                if (sessionError instanceof Exception) {
                    System.out.println("Exception type: " + sessionError.getClass().getName());
                    System.out.println("Exception message: " + ((Exception) sessionError).getMessage());
                    ((Exception) sessionError).printStackTrace();
                }
            }

            // Check OAuth2 specific error attributes
            Object oauth2ErrorAttr = request.getSession().getAttribute("org.springframework.security.oauth2.core.web.OAuth2AuthenticationException.ERROR");
            if (oauth2ErrorAttr != null) {
                System.out.println("OAuth2 Error attribute: " + oauth2ErrorAttr);
            }

            // Check all session attributes
            System.out.println("=== SESSION ATTRIBUTES ===");
            request.getSession().getAttributeNames().asIterator().forEachRemaining(attr -> {
                System.out.println("  " + attr + ": " + request.getSession().getAttribute(attr));
            });

            System.out.println("=== REQUEST PARAMETERS ===");
            request.getParameterNames().asIterator().forEachRemaining(param -> {
                System.out.println("  " + param + ": " + request.getParameter(param));
            });
        }

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