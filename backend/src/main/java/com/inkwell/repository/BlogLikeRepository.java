package com.inkwell.repository;

import com.inkwell.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogLikeRepository extends JpaRepository<BlogLike, BlogLike.BlogLikeId> {

    boolean existsByIdBlogIdAndIdUserId(Long blogId, Long userId);

    void deleteByIdBlogIdAndIdUserId(Long blogId, Long userId);
}
