-- Migration from 044 to 045

-- Add is_auto_generated_code column to sample_types
ALTER TABLE sample_types ADD COLUMN is_auto_generated_code BOOLEAN_CHAR NOT NULL DEFAULT 'F';
