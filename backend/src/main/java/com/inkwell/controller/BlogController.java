package com.inkwell.controller;

import com.inkwell.dto.BlogRequest;
import com.inkwell.dto.BlogResponse;
import com.inkwell.entity.User;
import com.inkwell.security.AuthenticatedUserProvider;
import com.inkwell.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.inkwell.repository.BlogLikeRepository;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogService blogService;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BlogLikeRepository blogLikeRepository;

    public BlogController(BlogService blogService, AuthenticatedUserProvider authenticatedUserProvider, BlogLikeRepository blogLikeRepository) {
        this.blogService = blogService;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.blogLikeRepository = blogLikeRepository;
    }

    @GetMapping("/{id}/like")
    public ResponseEntity<Map<String, Boolean>> likeStatus(@PathVariable Long id) {
        try {
            User currentUser = authenticatedUserProvider.getCurrentUser();
            return ResponseEntity.ok(Map.of("liked", blogLikeRepository.existsByIdBlogIdAndIdUserId(id, currentUser.getId())));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Map.of("liked", false));
        }
    }

    @GetMapping
    public ResponseEntity<Page<BlogResponse>> list(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        if (q != null && !q.isBlank()) {
            Pageable searchPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            return ResponseEntity.ok(blogService.search(q, searchPageable));
        }
        return ResponseEntity.ok(blogService.list(pageable));
    }

    @GetMapping("/top-weekly")
    public ResponseEntity<Page<BlogResponse>> topWeekly(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(blogService.topByEngagement(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BlogResponse> create(@Valid @RequestBody BlogRequest request) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody BlogRequest request) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(blogService.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        blogService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<BlogResponse> like(@PathVariable Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(blogService.like(id, currentUser));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<BlogResponse> unlike(@PathVariable Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(blogService.unlike(id, currentUser));
    }
}
