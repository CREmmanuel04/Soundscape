package com.example.soundscape.controllers;

import com.example.soundscape.models.Message;
import com.example.soundscape.models.User;
import com.example.soundscape.repositories.MessageRepository;
import com.example.soundscape.repositories.UserRepository;
import com.example.soundscape.services.FollowService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MessageController {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FollowService followService;

    public MessageController(UserRepository userRepository, MessageRepository messageRepository, FollowService followService) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.followService = followService;
    }

    // Page to show all users you can message (only mutual followers)
    @GetMapping("/messages")
    public String showMessageUserList(Model model, @AuthenticationPrincipal UserDetails currentUserDetails) {
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        
        List<User> allUsers = userRepository.findAll();
        // Filter to only show mutual followers
        List<User> mutualFollowers = allUsers.stream()
                .filter(user -> !user.getUsername().equals(currentUserDetails.getUsername())) // Remove current user
                .filter(user -> followService.areMutualFollowers(currentUser.getId(), user.getId()))
                .collect(Collectors.toList());
        
        model.addAttribute("users", mutualFollowers);
        return "messages"; // Renders messages.html
    }

    // Page to show the conversation with a specific user (only mutual followers)
    @GetMapping("/messages/{recipientUsername}")
    public String showConversation(
            @PathVariable String recipientUsername,
            Model model,
            @AuthenticationPrincipal UserDetails currentUserDetails,
            RedirectAttributes redirectAttributes) {

        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        User sender = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        // Check if users are mutual followers
        if (!followService.areMutualFollowers(sender.getId(), recipient.getId())) {
            redirectAttributes.addFlashAttribute("error", 
                "You can only message users who are mutual followers.");
            return "redirect:/messages";
        }

        // Fetch the conversation history
        List<Message> messages = messageRepository
                .findBySenderAndRecipientOrRecipientAndSenderOrderByCreatedAtAsc(sender, recipient, recipient, sender);

        model.addAttribute("recipient", recipient);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", sender);

        return "conversation"; // Renders conversation.html
    }

    // Handler to send a new message (only to mutual followers)
    @PostMapping("/messages/{recipientUsername}")
    public String sendMessage(
            @PathVariable String recipientUsername,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails currentUserDetails,
            RedirectAttributes redirectAttributes) {

        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        User sender = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        // Check if users are mutual followers
        if (!followService.areMutualFollowers(sender.getId(), recipient.getId())) {
            redirectAttributes.addFlashAttribute("error", 
                "You can only message users who are mutual followers.");
            return "redirect:/messages";
        }

        // Create and save the new message
        Message message = new Message(sender, recipient, content);
        messageRepository.save(message);

        // Redirect back to the conversation page to see the new message
        return "redirect:/messages/" + recipientUsername;
    }
}