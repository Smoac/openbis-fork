---------------------------------------------
-- Remove unused columns from TIME_SERIES
---------------------------------------------
ALTER TABLE TIME_SERIES DROP COLUMN BI_ID;
ALTER TABLE TIME_SERIES DROP COLUMN CG;

---------------------------------------------
-- Create index on CONTROLLED_GENE column
---------------------------------------------
CREATE INDEX TISE_CONTROLLED_GENE_ID ON TIME_SERIES (CONTROLLED_GENE);
