CREATE TABLE IF NOT EXISTS sessions (
    session_id varchar(255)  NOT NULL,
    session_user_id int NOT NULL,
    session_created_at bigint NOT NULL,
    PRIMARY KEY(session_id)
);
