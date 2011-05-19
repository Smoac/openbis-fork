#!/bin/bash
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/../env

# screening-specific
echo Creating empty screening database...
USER=`whoami`

$PSQL -U $DB_USER_NAME -c "create database $OPENBIS_DB with owner $USER template = template0 encoding = 'UNICODE'"
$PSQL -U $USER -d $OPENBIS_DB -f $BASE/empty-screening-database.sql