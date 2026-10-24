CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          user_name VARCHAR(255) NOT NULL UNIQUE,
                          email VARCHAR(255) NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP NOT NULL
);




CREATE TABLE category (
        id BIGSERIAL PRIMARY KEY,
       name VARCHAR(255) NOT NULL
);


CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    note VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    category_id BIGSERIAL REFERENCES category(id),
    user_id      BIGINT NOT NULL REFERENCES app_user(id),
    expense_date DATE NOT NULL
);

