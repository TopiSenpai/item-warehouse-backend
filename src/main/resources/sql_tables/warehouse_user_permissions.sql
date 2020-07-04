CREATE TABLE IF NOT EXISTS warehouse_user_permissions (
    wup_warehouse int NOT NULL,
    wup_user int NOT NULL,
    wup_permissions int NOT NULL,
    wup_updated_at bigint NOT NULL,
    wup_created_at bigint NOT NULL,
    PRIMARY KEY(wup_warehouse, wup_user)
);
