#!/bin/bash
# lists all queries which are currently executed by the database engine

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

$PSQL -U $DB_USER_NAME -d $OPENBIS_DB -c "select datname,procpid,usename,current_query,waiting,xact_start FROM pg_catalog.pg_stat_activity"