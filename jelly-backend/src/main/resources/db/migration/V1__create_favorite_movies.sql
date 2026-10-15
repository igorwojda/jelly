CREATE TABLE favorite_movies (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL,
    movie_id    BIGINT       NOT NULL,
    title       VARCHAR(512) NOT NULL,
    poster_path VARCHAR(255),
    added_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_favorite_movies_user_movie UNIQUE (user_id, movie_id)
);
