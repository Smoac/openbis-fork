#!/bin/bash
# lists all queries which are currently executed by the database engine

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

$PSQL -U $DB_USER_NAME -d $OPENBIS_DB -c "select datname,procpid,usename,current_query,waiting,xact_start FROM pg_catalog.pg_stat_activity"