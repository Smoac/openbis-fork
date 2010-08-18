DB=openbis_screening_dev
psql -U $DB -c "select datname,procpid,usename,current_query,waiting,xact_start FROM pg_catalog.pg_stat_activity" 
