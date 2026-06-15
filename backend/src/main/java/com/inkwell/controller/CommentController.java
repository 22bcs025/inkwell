package com.inkwell.controller;

import com.inkwell.dto.CommentRequest;
import com.inkwell.dto.CommentResponse;
import com.inkwell.entity.User;
import com.inkwell.security.AuthenticatedUserProvider;
import com.inkwell.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

    private final CommentService commentService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CommentController(CommentService commentService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.commentService = commentService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/api/blogs/{blogId}/comments")
    public ResponseEntity<Page<CommentResponse>> listByBlog(
            @PathVariable Long blogId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(commentService.listByBlog(blogId, pageable));
    }

    @PostMapping("/api/blogs/{blogId}/comments")
    public ResponseEntity<CommentResponse> create(@PathVariable Long blogId,
                                                   @Valid @RequestBody CommentRequest request) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        CommentResponse response = commentService.create(blogId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/comments/{id}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CommentRequest request) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(commentService.update(id, request, currentUser));
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        commentService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
