ALTER TABLE app_users ADD COLUMN deleted_at timestamptz;
CREATE INDEX idx_app_users_deleted_at ON app_users (deleted_at);

ALTER TABLE app_users DROP CONSTRAINT IF EXISTS app_users_email_key;

CREATE UNIQUE INDEX idx_app_users_email_unique ON app_users (email)
  WHERE deleted_at IS NULL;

ALTER TABLE households ADD COLUMN deleted_at timestamptz;
CREATE INDEX idx_households_deleted_at ON households (deleted_at);

ALTER TABLE household_members ADD COLUMN deleted_at timestamptz;
CREATE INDEX idx_household_members_deleted_at ON household_members (deleted_at);
