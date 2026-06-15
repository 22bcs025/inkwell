package com.inkwell.repository;

import com.inkwell.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "blog"})
    Page<Comment> findByBlogIdOrderByCreatedAtAsc(Long blogId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "blog"})
    Optional<Comment> findById(Long id);

    long countByBlogId(Long blogId);
}
