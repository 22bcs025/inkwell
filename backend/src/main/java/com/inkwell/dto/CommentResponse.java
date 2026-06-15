package com.inkwell.dto;

import com.inkwell.entity.Comment;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        Long blogId,
        Long authorId,
        String authorUsername,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getBlog().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
