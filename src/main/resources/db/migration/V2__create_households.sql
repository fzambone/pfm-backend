
CREATE TABLE households (
  id          UUID          PRIMARY KEY ,
  name        VARCHAR(255)  NOT NULL ,
  is_active   BOOLEAN       NOT NULL ,
  created_at  TIMESTAMPTZ   NOT NULL ,
  updated_at  TIMESTAMPTZ   NOT NULL ,
  created_by  UUID          NOT NULL ,
  updated_by  UUID          NOT NULL ,

  CONSTRAINT fk_app_user_created_by
    FOREIGN KEY (created_by)
    REFERENCES app_users (id) ,

  CONSTRAINT fk_app_user_updated_by
    FOREIGN KEY (updated_by)
    REFERENCES app_users (id)
);

CREATE TABLE household_members (
  household_id  UUID        NOT NULL ,
  app_user_id   UUID        NOT NULL ,
  role          VARCHAR(50) NOT NULL ,
  joined_at     TIMESTAMPTZ NOT NULL ,
  invited_by    UUID        NOT NULL ,

  PRIMARY KEY (household_id, app_user_id) ,

  CONSTRAINT chk_role_values CHECK (role IN ('ADMIN', 'MEMBER')) ,

  CONSTRAINT fk_household
    FOREIGN KEY (household_id)
    REFERENCES households (id)
    ON DELETE CASCADE ,

  CONSTRAINT fk_app_user
    FOREIGN KEY (app_user_id)
    REFERENCES app_users (id)
    ON DELETE CASCADE,

  CONSTRAINT fk_app_user_invited_by
    FOREIGN KEY (invited_by)
    REFERENCES app_users (id)
);
