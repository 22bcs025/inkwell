package com.inkwell.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "blog_likes")
public class BlogLike {

    @Embeddable
    public static class BlogLikeId implements Serializable {
        @Column(name = "blog_id")
        private Long blogId;
        @Column(name = "user_id")
        private Long userId;

        public BlogLikeId() {}
        public BlogLikeId(Long blogId, Long userId) {
            this.blogId = blogId;
            this.userId = userId;
        }
        public Long getBlogId() { return blogId; }
        public Long getUserId() { return userId; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlogLikeId that)) return false;
            return Objects.equals(blogId, that.blogId) && Objects.equals(userId, that.userId);
        }
        @Override
        public int hashCode() { return Objects.hash(blogId, userId); }
    }

    @EmbeddedId
    private BlogLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BlogLike() {}

    public BlogLike(Blog blog, User user) {
        this.blog = blog;
        this.user = user;
        this.id = new BlogLikeId(blog.getId(), user.getId());
    }

    @PrePersist
    protected void onCreate() { this.createdAt = Instant.now(); }

    public BlogLikeId getId() { return id; }
    public Blog getBlog() { return blog; }
    public User getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
}
