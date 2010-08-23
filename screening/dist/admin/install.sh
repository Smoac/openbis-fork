#!/bin/bash
# Installs openbis for screening for the first time. 
# openbis will be installed in the parent directory of the directory where this script is located. 

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

ROOT_DIR=$BASE/../servers
BACKUP_DIR=$BASE/../backup

mkdir -p $ROOT_DIR
mkdir -p $BACKUP_DIR

echo Restoring empty screening database
USER=`whoami`
psql -U postgres -c "create database $OPENBIS_DB with owner $USER template = template0 encoding = 'UNICODE'"
psql -U $USER -d $OPENBIS_DB -f $BASE/empty-screening-database.sql

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore*.zip -d $ROOT_DIR

echo Installing openBIS Application Server
unzip $ROOT_DIR/openBIS-*.zip -d $ROOT_DIR
$ROOT_DIR/openBIS-server/install.sh $ROOT_DIR/openBIS-server

mv $ROOT_DIR/*.zip $BACKUP_DIR/


