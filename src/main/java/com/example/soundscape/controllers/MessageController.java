package com.example.soundscape.controllers;

import com.example.soundscape.models.Message;
import com.example.soundscape.models.User;
import com.example.soundscape.repositories.MessageRepository;
import com.example.soundscape.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MessageController {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MessageController(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // Page to show all users you can message
    @GetMapping("/messages")
    public String showMessageUserList(Model model, @AuthenticationPrincipal UserDetails currentUserDetails) {
        List<User> allUsers = userRepository.findAll();
        // Remove the current user from the list so they can't message themselves
        allUsers.removeIf(user -> user.getUsername().equals(currentUserDetails.getUsername()));
        model.addAttribute("users", allUsers);
        return "messages"; // Renders messages.html
    }

    // Page to show the conversation with a specific user
    @GetMapping("/messages/{recipientUsername}")
    public String showConversation(
            @PathVariable String recipientUsername,
            Model model,
            @AuthenticationPrincipal UserDetails currentUserDetails) {

        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        User sender = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        // Fetch the conversation history
        List<Message> messages = messageRepository
                .findBySenderAndRecipientOrRecipientAndSenderOrderByCreatedAtAsc(sender, recipient, recipient, sender);

        model.addAttribute("recipient", recipient);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", sender);

        return "conversation"; // Renders conversation.html
    }

    // Handler to send a new message
    @PostMapping("/messages/{recipientUsername}")
    public String sendMessage(
            @PathVariable String recipientUsername,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails currentUserDetails) {

        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        User sender = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        // Create and save the new message
        Message message = new Message(sender, recipient, content);
        messageRepository.save(message);

        // Redirect back to the conversation page to see the new message
        return "redirect:/messages/" + recipientUsername;
    }
}