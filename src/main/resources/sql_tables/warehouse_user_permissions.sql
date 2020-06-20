CREATE TABLE IF NOT EXISTS warehouse_user_permissions (
    wup_warehouse int NOT NULL,
    wup_user varchar(255) NOT NULL,
    wup_permissions int NOT NULL,
    PRIMARY KEY(wup_warehouse, wup_user)
);
