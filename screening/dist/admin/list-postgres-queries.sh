#!/bin/bash
# lists all queries which are currently executed by the database engine

psql -U openbis -c "select datname,procpid,usename,current_query,waiting,xact_start FROM pg_catalog.pg_stat_activity" 
