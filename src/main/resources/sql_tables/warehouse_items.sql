CREATE TABLE IF NOT EXISTS warehouse_items (
    wi_id SERIAL NOT NULL,
    wi_name varchar(255) NOT NULL,
    wi_owner int NOT NULL,
    wi_count varchar(255) NOT NULL,
    wi_description varchar(255) NOT NULL,
    wi_storage_place int NOT NULL,
    wi_category int NOT NULL,
    wi_condition int NOT NULL,
    wi_image_path varchar(255) NOT NULL,
    wi_purchase_place varchar(255) NOT NULL,
    wi_purchase_price int NOT NULL,
    PRIMARY KEY(wi_id)
);
