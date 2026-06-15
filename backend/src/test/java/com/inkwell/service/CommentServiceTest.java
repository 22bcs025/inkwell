package com.inkwell.service;

import com.inkwell.dto.CommentRequest;
import com.inkwell.dto.CommentResponse;
import com.inkwell.entity.Blog;
import com.inkwell.entity.Comment;
import com.inkwell.entity.User;
import com.inkwell.exception.ForbiddenOperationException;
import com.inkwell.exception.ResourceNotFoundException;
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
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock BlogRepository blogRepository;
    @InjectMocks CommentService commentService;

    private User author;
    private Blog blog;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = new User("alice", "alice@example.com", "hash");
        author.setId(1L);
        blog = new Blog("Title", "Content", author);
        blog.setId(10L);
        comment = new Comment("Nice post", blog, author);
        comment.setId(20L);
    }

    @Test
    void create_savesComment() {
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse resp = commentService.create(10L, new CommentRequest("Nice post"), author);

        assertThat(resp.content()).isEqualTo("Nice post");
        assertThat(resp.authorUsername()).isEqualTo("alice");
    }

    @Test
    void create_blogNotFound_throws() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> commentService.create(99L, new CommentRequest("x"), author))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_byOwner_succeeds() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse resp = commentService.update(20L, new CommentRequest("Updated"), author);

        assertThat(resp).isNotNull();
    }

    @Test
    void update_byOtherUser_throws() {
        User other = new User("bob", "bob@example.com", "hash");
        other.setId(2L);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> commentService.update(20L, new CommentRequest("x"), other))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void delete_byOwner_succeeds() {
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        commentService.delete(20L, author);
        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_byOtherUser_throws() {
        User other = new User("bob", "bob@example.com", "hash");
        other.setId(2L);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));
        assertThatThrownBy(() -> commentService.delete(20L, other))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void listByBlog_blogNotFound_throws() {
        when(blogRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> commentService.listByBlog(99L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
