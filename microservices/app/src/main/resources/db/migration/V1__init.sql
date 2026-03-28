CREATE TABLE product (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255),
  description TEXT,
  attributes JSONB
);
