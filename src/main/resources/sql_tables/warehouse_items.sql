CREATE TABLE IF NOT EXISTS warehouse_items (
    wi_id serial NOT NULL,
    wi_warehouse int NOT NULL,
    wi_name varchar(255) NOT NULL,
    wi_owner int NOT NULL,
    wi_count varchar(255) NOT NULL,
    wi_description text NOT NULL,
    wi_storage_place varchar(255) NOT NULL,
    wi_category int NOT NULL,
    wi_condition int NOT NULL,
    wi_image_path varchar(255) NOT NULL,
    wi_purchase_place varchar(255) NOT NULL,
    wi_purchase_price int NOT NULL,
    wi_purchase_date bigint NOT NULL,
    wi_updated_at bigint NOT NULL,
    wi_created_at bigint NOT NULL,
    PRIMARY KEY(wi_id)
);
