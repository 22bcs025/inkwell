package com.inkwell.repository;

import com.inkwell.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    @Query(value = "SELECT b FROM Blog b LEFT JOIN FETCH b.author",
           countQuery = "SELECT count(b) FROM Blog b")
    Page<Blog> findAllWithAuthor(Pageable pageable);

    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.author WHERE b.id = :id")
    Optional<Blog> findById(@Param("id") Long id);

    @Query(value = """
       SELECT b.* FROM blogs b
       WHERE b.title ILIKE '%' || :query || '%'
          OR b.content ILIKE '%' || :query || '%'
          OR (length(:query) > 2 AND b.search_vector @@ websearch_to_tsquery('english', :query))
       ORDER BY
         CASE WHEN b.title ILIKE '%' || :query || '%' THEN 0 ELSE 1 END,
         b.created_at DESC
       """,
       countQuery = """
       SELECT count(*) FROM blogs b
       WHERE b.title ILIKE '%' || :query || '%'
          OR b.content ILIKE '%' || :query || '%'
          OR (length(:query) > 2 AND b.search_vector @@ websearch_to_tsquery('english', :query))
       """,
       nativeQuery = true)
Page<Blog> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.author ORDER BY (b.views + b.likes * 3) DESC, b.createdAt DESC")
    Page<Blog> findTopByEngagement(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Blog b SET b.views = b.views + 1 WHERE b.id = :id")
    void incrementViews(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Blog b SET b.likes = b.likes + 1 WHERE b.id = :id")
    void incrementLikes(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Blog b SET b.likes = b.likes - 1 WHERE b.id = :id AND b.likes > 0")
    void decrementLikes(@Param("id") Long id);

    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.author WHERE b.id IN :ids")
       List<Blog> findAllWithAuthorByIds(@Param("ids") List<Long> ids);

    long countByAuthorId(Long authorId);
}
