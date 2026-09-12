-- Repairs environments baselined from the pre-Flyway, Hibernate-managed schema,
-- where `description` ended up as TINYTEXT (255-byte cap) instead of TEXT.
ALTER TABLE category MODIFY description TEXT;
ALTER TABLE product MODIFY description TEXT;
