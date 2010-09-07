#!/bin/bash
DB=openbis_cina
USER=cramakri
psql -U $USER -h localhost -p 5432 -d $DB -f cina_metadata.sql
