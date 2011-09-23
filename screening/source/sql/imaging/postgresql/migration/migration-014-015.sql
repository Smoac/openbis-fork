-- Migration from 014 to 015

alter table IMAGE_TRANSFORMATIONS add column IS_DEFAULT BOOLEAN_CHAR NOT NULL DEFAULT 'F';

CREATE OR REPLACE FUNCTION IMAGE_TRANSFORMATIONS_DEFAULT_CHECK() RETURNS trigger AS $$
DECLARE
   v_is_default boolean;
BEGIN
   if NEW.is_default = 'T' then
	   select is_default into v_is_default from IMAGE_TRANSFORMATIONS 
	   	where is_default = 'T' 
	   			  and channel_id = NEW.channel_id
	   				and id != NEW.id;
	   if v_is_default is NOT NULL then
	      RAISE EXCEPTION 'Insert/Update of image transformation (Code: %) failed, as the new record has is_default set to true and there is already a default record defined.', NEW.code;
	   end if;
   end if;

   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER IMAGE_TRANSFORMATIONS_DEFAULT_CHECK BEFORE INSERT OR UPDATE ON IMAGE_TRANSFORMATIONS
    FOR EACH ROW EXECUTE PROCEDURE IMAGE_TRANSFORMATIONS_DEFAULT_CHECK();
