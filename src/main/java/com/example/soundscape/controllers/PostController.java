package com.example.soundscape.controllers;

import com.example.soundscape.models.Post;
import com.example.soundscape.models.PostView;
import com.example.soundscape.models.User;
import com.example.soundscape.services.UserService;
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
    private final UserService userService;

    public PostController(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @GetMapping("/posts")
    public String postsPage(Model model) {
        // Convert Post -> PostView (adds avatar url)
        var posts = postRepository.findAllByOrderByCreatedAtDesc();
        var views = posts.stream().map(p -> {
            String avatar = null;
            try {
                User u = userService.findByUsername(p.getAuthor());
                avatar = u.getProfilePicture();
            } catch (RuntimeException e) {
                // user not found, leave avatar null
            }
            return new PostView(p, avatar);
        }).toList();

        model.addAttribute("posts", views);
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
