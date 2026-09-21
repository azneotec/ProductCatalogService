-- Lean catalog model: category is id + name only, product drops the unused
-- sale flag and gains the FakeStore rating (rate, count).
ALTER TABLE category DROP COLUMN description;
ALTER TABLE product DROP COLUMN is_sale_eligible;
ALTER TABLE product ADD COLUMN rating_rate DOUBLE NULL;
ALTER TABLE product ADD COLUMN rating_count INT NULL;
