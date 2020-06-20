CREATE TABLE IF NOT EXISTS warehouse_categories (
    wc_id SERIAL NOT NULL,
    wc_warehouse int NOT NULL,
    wc_name int NOT NULL,
    PRIMARY KEY(wc_id, wc_warehouse)
);
