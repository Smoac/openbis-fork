-----------------------------------
-- Migration 002-003
-----------------------------------

-- Create Table EVENTS

CREATE TABLE EVENTS (
  LAST_SEEN_DELETION_EVENT_ID TECH_ID NOT NULL
);

-- Create trigger deleting images unused in acquired_images

CREATE OR REPLACE FUNCTION DELETE_UNUSED_IMAGES() RETURNS trigger AS $$
BEGIN
   delete from images where id not in (select i.id from acquired_images ai, images i where (i.id=ai.img_id or ai.thumbnail_id = i.id));
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UNUSED_IMAGES AFTER DELETE ON ACQUIRED_IMAGES
    FOR EACH ROW EXECUTE PROCEDURE DELETE_UNUSED_IMAGES();
