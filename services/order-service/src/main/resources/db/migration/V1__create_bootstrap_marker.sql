CREATE TABLE bootstrap_marker (
    id BIGINT PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO bootstrap_marker (id, created_at)
VALUES (1, CURRENT_TIMESTAMP);
