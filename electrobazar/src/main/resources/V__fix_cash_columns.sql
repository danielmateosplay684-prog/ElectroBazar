-- Migration to fix naming inconsistency and cleanup unused columns
-- 1. Rename column in sales to match CashRegister entity mapping
ALTER TABLE sales RENAME COLUMN cash_session_id TO cash_register_id;

-- 2. Remove orphaned column in cash_withdrawals that causes "Field doesn't have a default value"
-- This column is not present in the Java entity but exists in the physical database
ALTER TABLE cash_withdrawals DROP COLUMN IF EXISTS cash_session_id;
