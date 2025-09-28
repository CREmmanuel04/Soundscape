package com.example.soundscape.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostController {
    @GetMapping("/posts")
    public String postsPage() {
        return "posts"; // Looks for posts.html
    }

    // Add this to handle the form submission
    @PostMapping("/posts")
    public String createPost(@RequestParam String content) {
        System.out.println("Received post: " + content);
        return "redirect:/posts"; // Refresh the page after posting
    }
}
