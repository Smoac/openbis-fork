-----------------------------------
-- Migration 001-002
-----------------------------------

-- Create Table EVENTS

CREATE TABLE EVENTS (
  EVENT_DATE TIMESTAMP WITH TIME ZONE NOT NULL
);

GRANT SELECT ON TABLE EVENTS TO GROUP metabol_readonly;
GRANT ALL PRIVILEGES ON TABLE EVENTS TO GROUP metabol_readwrite;
