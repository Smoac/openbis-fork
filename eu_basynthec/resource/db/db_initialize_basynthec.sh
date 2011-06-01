#!/bin/bash
DB=openbis_basynthec
USER=cramakri
psql -U $USER -h localhost -p 5432 -d $DB -f basynthec_metadata.sql
