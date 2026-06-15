package com.inkwell.service;
import com.inkwell.repository.UserRepository;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final UserRepository userRepository;

    public BlogService(BlogRepository blogRepository,
                    CommentRepository commentRepository,
                    BlogLikeRepository blogLikeRepository,
                    UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.blogLikeRepository = blogLikeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<BlogResponse> list(Pageable pageable) {
        return blogRepository.findAllWithAuthor(pageable)
                .map(blog -> BlogResponse.from(
                        blog,
                        commentRepository.countByBlogId(blog.getId())
                ));
    }

   @Transactional(readOnly = true)
    public Page<BlogResponse> search(String query, Pageable pageable) {
        Page<Blog> results = blogRepository.search(query, pageable);
        List<Long> ids = results.getContent().stream()
                .map(Blog::getId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return results.map(this::toResponse);
        Map<Long, Blog> blogsWithAuthor = blogRepository.findAllWithAuthorByIds(ids).stream()
                .collect(Collectors.toMap(Blog::getId, b -> b));
        return results.map(b -> toResponse(blogsWithAuthor.getOrDefault(b.getId(), b)));
    }

    @Transactional(readOnly = true)
    public Page<BlogResponse> topByEngagement(Pageable pageable) {
        return blogRepository.findTopByEngagement(pageable).map(this::toResponse);
    }

    @Transactional
    public BlogResponse getById(Long id) {
        blogRepository.incrementViews(id);
        Blog blog = findBlogOrThrow(id);
        return BlogResponse.from(blog, commentRepository.countByBlogId(id));
    }

    @Transactional
    public BlogResponse create(BlogRequest request, User author) {
        Blog blog = new Blog(request.title(), request.content(), author);
        return toResponse(blogRepository.save(blog));
    }

    @Transactional
    public BlogResponse update(Long id, BlogRequest request, User currentUser) {
        Blog blog = findBlogOrThrow(id);
        if (!blog.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only edit your own blogs");
        }
        blog.setTitle(request.title());
        blog.setContent(request.content());
        return toResponse(blogRepository.save(blog));
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Blog blog = findBlogOrThrow(id);
        if (!blog.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only delete your own blogs");
        }
        blogRepository.delete(blog);
    }

    @Transactional
    public BlogResponse like(Long blogId, User currentUser) {
        Blog blog = findBlogOrThrow(blogId);
        if (blogLikeRepository.existsByIdBlogIdAndIdUserId(blogId, currentUser.getId())) {
            throw new DuplicateResourceException("You have already liked this blog");
        }
        blogLikeRepository.save(new BlogLike(blog, currentUser));
        blogRepository.incrementLikes(blogId);
        return toResponse(findBlogOrThrow(blogId));
    }

    @Transactional
    public BlogResponse unlike(Long blogId, User currentUser) {
        findBlogOrThrow(blogId);
        if (!blogLikeRepository.existsByIdBlogIdAndIdUserId(blogId, currentUser.getId())) {
            throw new ResourceNotFoundException("You have not liked this blog");
        }
        blogLikeRepository.deleteByIdBlogIdAndIdUserId(blogId, currentUser.getId());
        blogRepository.decrementLikes(blogId);
        return toResponse(findBlogOrThrow(blogId));
    }

    private Blog findBlogOrThrow(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found: " + id));
    }

    private BlogResponse toResponse(Blog blog) {
        return BlogResponse.from(blog, commentRepository.countByBlogId(blog.getId()));
    }
}
