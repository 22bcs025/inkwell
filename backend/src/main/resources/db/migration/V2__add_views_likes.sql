ALTER TABLE blogs ADD COLUMN views BIGINT NOT NULL DEFAULT 0;
ALTER TABLE blogs ADD COLUMN likes BIGINT NOT NULL DEFAULT 0;

CREATE TABLE blog_likes (
    blog_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blog_id, user_id),
    CONSTRAINT fk_blog_likes_blog FOREIGN KEY (blog_id) REFERENCES blogs(id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
