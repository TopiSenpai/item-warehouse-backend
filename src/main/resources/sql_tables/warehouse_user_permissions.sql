CREATE TABLE IF NOT EXISTS warehouses (
    warehouse_id SERIAL NOT NULL,
    warehouse_name varchar(255) NOT NULL,
    warehouse_owner int NOT NULL,
    salt varchar(255) NOT NULL,
    PRIMARY KEY(id)
);
