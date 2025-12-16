
CREATE TABLE app_users (
  id            UUID          PRIMARY KEY ,
  email         VARCHAR(255)  NOT NULL UNIQUE ,
  password_hash VARCHAR(255)  NOT NULL ,
  full_name     VARCHAR(255)  NOT NULL ,
  created_at    TIMESTAMPTZ   NOT NULL ,
  updated_at    TIMESTAMPTZ   NOT NULL ,
  created_by    UUID          NOT NULL ,
  updated_by    UUID          NOT NULL ,

  CONSTRAINT fk_app_user_created_by
    FOREIGN KEY (created_by)
    REFERENCES app_users (id) ,

  CONSTRAINT fk_app_user_updated_by
    FOREIGN KEY (updated_by)
    REFERENCES app_users (id)
);
