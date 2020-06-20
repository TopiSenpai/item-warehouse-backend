CREATE TABLE IF NOT EXISTS users (
    id SERIAL  NOT NULL,
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    logged_in boolean NOT NULL,
    PRIMARY KEY(id)
);
