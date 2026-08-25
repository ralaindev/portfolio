DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'inventory') THEN
        CREATE ROLE inventory LOGIN PASSWORD 'inventory';
    END IF;
END
$$;

CREATE DATABASE inventory_db OWNER inventory;
