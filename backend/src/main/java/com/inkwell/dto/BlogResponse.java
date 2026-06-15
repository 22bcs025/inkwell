package com.inkwell.dto;

import com.inkwell.entity.Blog;

import java.time.Instant;

public record BlogResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorUsername,
        Instant createdAt,
        Instant updatedAt,
        long commentCount,
        long views,
        long likes
) {
    public static BlogResponse from(Blog blog, long commentCount) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getAuthor().getId(),
                blog.getAuthor().getUsername(),
                blog.getCreatedAt(),
                blog.getUpdatedAt(),
                commentCount,
                blog.getViews(),
                blog.getLikes()
        );
    }
}
