
CREATE TABLE accounts (
  id            UUID        PRIMARY KEY ,
  household_id  UUID        NOT NULL ,
  name          VARCHAR (255) NOT NULL ,
  account_type  VARCHAR (50)  NOT NULL ,
  currency_code VARCHAR (3)   NOT NULL ,
  current_balance DECIMAL     DEFAULT 0.00 ,
  is_active       BOOLEAN     NOT NULL ,
  created_at      TIMESTAMPTZ NOT NULL ,
  updated_at      TIMESTAMPTZ NOT NULL ,
  deleted_at      TIMESTAMPTZ ,
  created_by      UUID        NOT NULL ,
  updated_by      UUID        NOT NULL ,

  CONSTRAINT chk_account_type_values CHECK (
    account_type IN ('CHECKING', 'SAVINGS', 'CREDIT_CARD', 'INVESTMENT')) ,

  CONSTRAINT chk_currency_code_values CHECK (
    currency_code IN ('USD', 'BRL', 'EUR')) ,

  CONSTRAINT fk_app_user_created_by
    FOREIGN KEY (created_by)
    REFERENCES app_users (id) ,

  CONSTRAINT fk_app_user_updated_by
    FOREIGN KEY (updated_by)
      REFERENCES app_users (id)
);
