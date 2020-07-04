CREATE TABLE IF NOT EXISTS warehouses (
    warehouse_id SERIAL NOT NULL,
    warehouse_name varchar(255) NOT NULL,
    warehouse_updated_at bigint NOT NULL,
    warehouse_created_at bigint NOT NULL,
    PRIMARY KEY(warehouse_id)
);
