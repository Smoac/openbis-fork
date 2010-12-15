-- Migration from 062 to 063

-- connect wells to experiments of their plates (screening specific)  
UPDATE samples 
	SET expe_id = sc.expe_id
	FROM samples sc, sample_types sct
	WHERE samples.expe_id IS NULL
	AND sc.id = samples.samp_id_part_of
	AND sct.id = sc.saty_id AND sct.code = 'PLATE';

