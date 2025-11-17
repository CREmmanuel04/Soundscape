package com.example.soundscape.repositories;

import com.example.soundscape.models.Message;
import com.example.soundscape.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages between two users (both directions), ordered by when they were created
    List<Message> findBySenderAndRecipientOrSenderAndRecipientOrderByCreatedAtAsc(
            User sender1, User recipient1, User sender2, User recipient2);
}