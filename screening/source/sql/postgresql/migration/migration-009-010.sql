-- Migration from 009 to 010

ALTER TABLE images ADD COLUMN image_transformer_factory BYTEA;