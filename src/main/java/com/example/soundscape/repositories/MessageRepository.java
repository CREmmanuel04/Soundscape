package com.example.soundscape.repositories;

import com.example.soundscape.models.Message;
import com.example.soundscape.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages between two users, ordered by when they were created
    List<Message> findBySenderAndRecipientOrRecipientAndSenderOrderByCreatedAtAsc(
            User user1, User user2, User user3, User user4);
}