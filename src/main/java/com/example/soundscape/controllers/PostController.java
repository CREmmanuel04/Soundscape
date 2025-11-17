package com.example.soundscape.controllers;

import com.example.soundscape.models.Post;
import com.example.soundscape.models.User;
import com.example.soundscape.repositories.PostRepository;
import com.example.soundscape.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class PostController {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostController(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/posts")
    public String postsPage(Model model) {
        model.addAttribute("posts", postRepository.findAllByOrderByCreatedAtDesc());
        return "posts";
    }

    @PostMapping("/posts")
    public String createPost(@RequestParam String content, @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = new Post(content, username, userEntity);
        postRepository.save(post);
        return "redirect:/posts";
    }

    @GetMapping("/posts/search")
    public String searchPosts(@RequestParam String q, Model model) {

        List<Post> posts;
        String queryPrefix = "from:";

        if (q.startsWith(queryPrefix)) {
            // This is an author search
            String authorName = q.substring(queryPrefix.length()).trim();
            posts = postRepository.findByAuthorContainingIgnoreCaseOrderByCreatedAtDesc(authorName);

            // Tweak the search query for display
            model.addAttribute("searchQuery", "Posts from " + authorName);

        } else {
            // This is a normal content search
            posts = postRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(q);
            model.addAttribute("searchQuery", q);
        }

        model.addAttribute("posts", posts);
        return "posts"; // Reuse same template
    }

    @PostMapping("/posts/{id}/like")
    public String likePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Post post = postRepository.findById(id).orElseThrow();
        String username = user.getUsername();

        if (post.isLikedBy(username)) {
            // UNLIKE: User already liked, so remove like
            post.removeLike(username);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // LIKE: User hasn't liked yet, so add like
            post.addLike(username);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepository.save(post);
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Post post = postRepository.findById(id).orElseThrow();

        // Only allow author to delete
        if (post.getAuthor().equals(user.getUsername())) {
            postRepository.delete(post);
        }

        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/edit")
    public String editPost(@PathVariable Long id, @RequestParam String content,
                           @AuthenticationPrincipal UserDetails user) {
        Post post = postRepository.findById(id).orElseThrow();

        // Only allow author to edit
        if (post.getAuthor().equals(user.getUsername())) {
            post.updateContent(content);
            postRepository.save(post);
        }

        return "redirect:/posts";
    }

    // ADD THIS TIME FORMATTER BEAN
    @org.springframework.context.annotation.Bean
    public TimeFormatter timeFormatter() {
        return new TimeFormatter();
    }

    // ADD THIS INNER CLASS FOR TIME FORMATTING
    public static class TimeFormatter {
        public String format(Instant instant) {
            LocalDateTime postTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            LocalDateTime now = LocalDateTime.now();

            long seconds = ChronoUnit.SECONDS.between(postTime, now);
            long minutes = ChronoUnit.MINUTES.between(postTime, now);
            long hours = ChronoUnit.HOURS.between(postTime, now);
            long days = ChronoUnit.DAYS.between(postTime, now);

            if (seconds < 5) return "Just now";
            if (seconds < 60) return seconds + "s ago";
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days == 1) return "Yesterday";
            if (days < 7) return days + "d ago";

            return java.time.format.DateTimeFormatter.ofPattern("MMM dd").format(postTime);
        }
    }
}