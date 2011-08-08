-- Migration from 078 to 079

--
--  Allow to storing multiple entity deletions in the same event row
--
ALTER TABLE events RENAME identifier TO identifiers;
ALTER TABLE events ALTER COLUMN identifiers TYPE TEXT_VALUE;
ALTER TABLE events ALTER COLUMN description TYPE TEXT_VALUE;