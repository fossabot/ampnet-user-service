ALTER TABLE bank_account DROP COLUMN format;
ALTER TABLE bank_account RENAME COLUMN account TO iban;
ALTER TABLE bank_account ADD COLUMN bank_code VARCHAR(16) NOT NULL DEFAULT 'missing';
