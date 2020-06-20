CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL  NOT NULL,
    user_name varchar(255) NOT NULL,
    user_password varchar(255) NOT NULL,
    user_salt varchar(255) NOT NULL,
    PRIMARY KEY(user_id)
);
