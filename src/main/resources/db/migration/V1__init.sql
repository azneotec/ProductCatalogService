CREATE TABLE category (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at       DATETIME(6),
    last_updated_at  DATETIME(6),
    state            TINYINT CHECK (state BETWEEN 0 AND 1),
    name             VARCHAR(255),
    description      TEXT
);

CREATE TABLE product (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at         DATETIME(6),
    last_updated_at    DATETIME(6),
    state              TINYINT CHECK (state BETWEEN 0 AND 1),
    title              VARCHAR(255),
    description        TEXT,
    image_url          VARCHAR(255),
    price              FLOAT,
    is_sale_eligible   BIT(1),
    category_id        BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id)
);
