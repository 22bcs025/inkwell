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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;

    public CommentService(CommentRepository commentRepository, BlogRepository blogRepository) {
        this.commentRepository = commentRepository;
        this.blogRepository = blogRepository;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> listByBlog(Long blogId, Pageable pageable) {
        if (!blogRepository.existsById(blogId)) {
            throw new ResourceNotFoundException("Blog not found: " + blogId);
        }
        return commentRepository.findByBlogIdOrderByCreatedAtAsc(blogId, pageable)
                .map(CommentResponse::from);
    }

    @Transactional
    public CommentResponse create(Long blogId, CommentRequest request, User author) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found: " + blogId));

        Comment comment = new Comment(request.content(), blog, author);
        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    @Transactional
    public CommentResponse update(Long commentId, CommentRequest request, User currentUser) {
        Comment comment = findCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only edit your own comments");
        }

        comment.setContent(request.content());
        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    @Transactional
    public void delete(Long commentId, User currentUser) {
        Comment comment = findCommentOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private Comment findCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));
    }
}
