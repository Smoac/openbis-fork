#!/bin/bash
DB=openbis_basynthec
psql -U $USER -h localhost -p 5432 -d $DB -f basynthec_metadata.sql
