package com.example.soundscape.repositories;

import com.example.soundscape.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    // Find user by username (for login)
    Optional<User> findByUsername(String username);

    // Find user by email (for registration checking)
    Optional<User> findByEmail(String email);

    // Check if username and email exist (for registration validation)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
