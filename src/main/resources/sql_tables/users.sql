CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL  NOT NULL,
    user_name varchar(255) NOT NULL,
    user_password varchar(255) NOT NULL,
    user_salt varchar(255) NOT NULL,
    user_updated_at bigint NOT NULL,
    user_created_at bigint NOT NULL,
    PRIMARY KEY(user_id, user_name)
);
