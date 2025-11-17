package com.example.soundscape.repositories;

import com.example.soundscape.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String searchTerm);

    List<Post> findByAuthorContainingIgnoreCaseOrderByCreatedAtDesc(String author);
}
