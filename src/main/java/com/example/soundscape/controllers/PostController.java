package com.example.soundscape.controllers;

import com.example.soundscape.models.Post;
import com.example.soundscape.repositories.PostRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PostController {
    private PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/posts")
    public String postsPage(Model model) {
        // Get all posts from that database and pass to the posts template
        model.addAttribute("posts", postRepository.findAllByOrderByCreatedAtDesc());
        return "posts"; // Looks for posts.html
    }

    // Add this to handle the form submission
    @PostMapping("/posts")
    public String createPost(@RequestParam String content, @AuthenticationPrincipal UserDetails user) {
        String author = user.getUsername(); // Get logged-in username
        Post post = new Post(content, author);
        postRepository.save(post); // Save to H2

        System.out.println("DEBUG | Received post: " + content); // DEBUG PRINT
        return "redirect:/posts"; // Refresh the page after posting
    }
}
