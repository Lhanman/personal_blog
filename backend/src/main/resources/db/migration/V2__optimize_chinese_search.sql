CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP INDEX IF EXISTS posts_search_idx;

ALTER TABLE posts DROP COLUMN IF EXISTS search_vector;
ALTER TABLE posts DROP COLUMN IF EXISTS search_text;

ALTER TABLE posts ADD COLUMN search_text TEXT GENERATED ALWAYS AS (
    lower(regexp_replace(concat_ws(' ', coalesce(title, ''), coalesce(summary, ''), coalesce(content, '')), '\s+', ' ', 'g'))
) STORED;

ALTER TABLE posts ADD COLUMN search_vector tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('simple', coalesce(summary, '')), 'B') ||
    setweight(to_tsvector('simple', coalesce(content, '')), 'C')
) STORED;

CREATE INDEX IF NOT EXISTS posts_search_vector_idx ON posts USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS posts_search_text_trgm_idx ON posts USING GIN (search_text gin_trgm_ops);
CREATE INDEX IF NOT EXISTS posts_published_created_idx ON posts (published, created_at DESC);
