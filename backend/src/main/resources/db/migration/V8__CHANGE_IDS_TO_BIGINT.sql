-- Change all IDs to BIGINT to support Snowflake IDs (64-bit)
-- We also remove the default SERIAL/identity sequences to allow the application to provide the IDs

-- Tables: users, units, categories, items, lists, list_items, list_user_permissions

-- Users
ALTER TABLE users ALTER COLUMN id TYPE BIGINT;
ALTER TABLE users ALTER COLUMN id DROP DEFAULT;

-- Units
ALTER TABLE units ALTER COLUMN id TYPE BIGINT;
ALTER TABLE units ALTER COLUMN id DROP DEFAULT;

-- Categories
ALTER TABLE categories ALTER COLUMN id TYPE BIGINT;
ALTER TABLE categories ALTER COLUMN id DROP DEFAULT;

-- Items
ALTER TABLE items ALTER COLUMN id TYPE BIGINT;
ALTER TABLE items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE items ALTER COLUMN id_category TYPE BIGINT;
ALTER TABLE items ALTER COLUMN id_unit TYPE BIGINT;

-- Lists
ALTER TABLE lists ALTER COLUMN id TYPE BIGINT;
ALTER TABLE lists ALTER COLUMN id DROP DEFAULT;
ALTER TABLE lists ALTER COLUMN owner_id_user TYPE BIGINT;

-- List Items
ALTER TABLE list_items ALTER COLUMN id TYPE BIGINT;
ALTER TABLE list_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE list_items ALTER COLUMN id_list TYPE BIGINT;
ALTER TABLE list_items ALTER COLUMN id_item TYPE BIGINT;

-- List User Permissions
ALTER TABLE list_user_permissions ALTER COLUMN id TYPE BIGINT;
ALTER TABLE list_user_permissions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE list_user_permissions ALTER COLUMN id_list TYPE BIGINT;
ALTER TABLE list_user_permissions ALTER COLUMN id_user TYPE BIGINT;
