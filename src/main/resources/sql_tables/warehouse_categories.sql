CREATE TABLE IF NOT EXISTS warehouse_items (
    wi_item SERIAL NOT NULL,
    wi_owner int NOT NULL,
    wi_name varchar(255) NOT NULL,
    wi_description varchar(255) NOT NULL,
    wi_category varchar(255) NOT NULL,
    PRIMARY KEY(wi_item)
);
