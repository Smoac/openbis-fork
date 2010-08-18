-- Migration from 005 to 006


-- Remove references to redundant channel stacks
update acquired_images set channel_stack_id = mapping.new from (select cs.id as old, ucs.max_id as new from (select  max(id) as max_id,x,y,spot_id,ds_id from channel_stacks group by x,y,spot_id,ds_id) as ucs, channel_stacks cs where cs.x = ucs.x and cs.y = ucs.y and cs.spot_id = ucs.spot_id and cs.ds_id = ucs.ds_id and not cs.id = ucs.max_id) as mapping where mapping.old = channel_stack_id;

-- Remove redundant channel stacks
delete from channel_stacks where id not in (select  max(id) as max_id from channel_stacks group by x,y,spot_id,ds_id);


ALTER TABLE CHANNELS ADD COLUMN CODE NAME;
ALTER TABLE CHANNELS DROP CONSTRAINT CHANNELS_UK_1;
ALTER TABLE CHANNELS DROP CONSTRAINT CHANNELS_UK_2;
UPDATE CHANNELS SET CODE = regexp_replace(upper(NAME), '[^A-Z0-9]', '_', 'g');
ALTER TABLE CHANNELS RENAME COLUMN NAME TO LABEL;
ALTER TABLE CHANNELS ALTER COLUMN CODE SET NOT NULL;
ALTER TABLE CHANNELS ADD CONSTRAINT CHANNELS_UK_1 UNIQUE(CODE, DS_ID);
ALTER TABLE CHANNELS ADD CONSTRAINT CHANNELS_UK_2 UNIQUE(CODE, EXP_ID);

ALTER TABLE FEATURE_DEFS ADD COLUMN CODE NAME;
ALTER TABLE FEATURE_DEFS DROP CONSTRAINT FEATURE_DEFS_UK_1;
UPDATE FEATURE_DEFS SET CODE = regexp_replace(upper(NAME), '[^A-Z0-9]', '_', 'g');
ALTER TABLE FEATURE_DEFS RENAME COLUMN NAME TO LABEL;
ALTER TABLE FEATURE_DEFS ALTER COLUMN CODE SET NOT NULL;
ALTER TABLE FEATURE_DEFS ADD CONSTRAINT FEATURE_DEFS_UK_1 UNIQUE(CODE, DS_ID);
