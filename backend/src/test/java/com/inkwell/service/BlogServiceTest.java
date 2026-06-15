package com.inkwell.service;

import com.inkwell.dto.BlogRequest;
import com.inkwell.dto.BlogResponse;
import com.inkwell.entity.Blog;
import com.inkwell.entity.BlogLike;
import com.inkwell.entity.User;
import com.inkwell.exception.DuplicateResourceException;
import com.inkwell.exception.ForbiddenOperationException;
import com.inkwell.exception.ResourceNotFoundException;
import com.inkwell.repository.BlogLikeRepository;
import com.inkwell.repository.BlogRepository;
import com.inkwell.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock BlogRepository blogRepository;
    @Mock CommentRepository commentRepository;
    @Mock BlogLikeRepository blogLikeRepository;
    @InjectMocks BlogService blogService;

    private User author;
    private Blog blog;

    @BeforeEach
    void setUp() {
        author = new User("alice", "alice@example.com", "hash");
        author.setId(1L);
        blog = new Blog("Title", "Content", author);
        blog.setId(10L);
    }

    @Test
    void create_savesAndReturnsBlog() {
        when(blogRepository.save(any(Blog.class))).thenReturn(blog);
        when(commentRepository.countByBlogId(10L)).thenReturn(0L);

        BlogResponse resp = blogService.create(new BlogRequest("Title", "Content"), author);

        assertThat(resp.title()).isEqualTo("Title");
        assertThat(resp.authorUsername()).isEqualTo("alice");
    }

    @Test
    void getById_incrementsViews() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(commentRepository.countByBlogId(10L)).thenReturn(2L);

        BlogResponse resp = blogService.getById(10L);

        verify(blogRepository).incrementViews(10L);
        assertThat(resp.commentCount()).isEqualTo(2L);
    }

    @Test
    void getById_notFound_throws() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> blogService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_byOwner_succeeds() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(blogRepository.save(any(Blog.class))).thenReturn(blog);
        when(commentRepository.countByBlogId(10L)).thenReturn(0L);

        BlogResponse resp = blogService.update(10L, new BlogRequest("New", "Body"), author);

        assertThat(resp).isNotNull();
        verify(blogRepository).save(blog);
    }

    @Test
    void update_byOtherUser_throws() {
        User other = new User("bob", "bob@example.com", "hash");
        other.setId(2L);
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));

        assertThatThrownBy(() -> blogService.update(10L, new BlogRequest("X", "Y"), other))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void delete_byOwner_succeeds() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        blogService.delete(10L, author);
        verify(blogRepository).delete(blog);
    }

    @Test
    void delete_byOtherUser_throws() {
        User other = new User("bob", "bob@example.com", "hash");
        other.setId(2L);
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        assertThatThrownBy(() -> blogService.delete(10L, other))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void like_newLike_succeeds() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(blogLikeRepository.existsByIdBlogIdAndIdUserId(10L, 1L)).thenReturn(false);
        when(blogLikeRepository.save(any(BlogLike.class))).thenReturn(new BlogLike(blog, author));
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(commentRepository.countByBlogId(10L)).thenReturn(0L);

        blogService.like(10L, author);

        verify(blogRepository).incrementLikes(10L);
    }

    @Test
    void like_alreadyLiked_throws() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(blogLikeRepository.existsByIdBlogIdAndIdUserId(10L, 1L)).thenReturn(true);
        assertThatThrownBy(() -> blogService.like(10L, author))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void unlike_existingLike_succeeds() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(blogLikeRepository.existsByIdBlogIdAndIdUserId(10L, 1L)).thenReturn(true);
        when(commentRepository.countByBlogId(10L)).thenReturn(0L);

        blogService.unlike(10L, author);

        verify(blogRepository).decrementLikes(10L);
    }

    @Test
    void unlike_notLiked_throws() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(blogLikeRepository.existsByIdBlogIdAndIdUserId(10L, 1L)).thenReturn(false);
        assertThatThrownBy(() -> blogService.unlike(10L, author))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
