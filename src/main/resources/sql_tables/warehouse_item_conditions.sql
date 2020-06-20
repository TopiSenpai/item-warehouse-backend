CREATE TABLE IF NOT EXISTS warehouse_item_conditions (
    wic_id SERIAL NOT NULL,
    wic_warehouse int NOT NULL,
    wic_name int NOT NULL,
    PRIMARY KEY(wic_id, wic_warehouse)
);
