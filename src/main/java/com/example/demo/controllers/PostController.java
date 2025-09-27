package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {
    @GetMapping("/posts")
    public String postsPage() {
        return "posts"; // Looks for posts.html
    }
}
