package com.example.demo.post;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String feed(Model model) {
        model.addAttribute("posts", postRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("postForm", new PostForm());
        return "feed";
    }

    @PostMapping("/posts")
    @Transactional
    public String createPost(@ModelAttribute("postForm") PostForm postForm,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails user) {
        if (bindingResult.hasErrors() || postForm.content() == null || postForm.content().isBlank()) {
            return "redirect:/";
        }
        String author = (user != null ? user.getUsername() : "anonymous");
        String trimmed = postForm.content().trim();
        if (trimmed.length() > 280) {
            trimmed = trimmed.substring(0, 280);
        }
        postRepository.save(new Post(trimmed, author));
        return "redirect:/";
    }

    public static class PostForm {
        @NotBlank
        @Size(max = 280)
        private String content;

        public PostForm() { }

        public PostForm(String content) {
            this.content = content;
        }

        public String content() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}