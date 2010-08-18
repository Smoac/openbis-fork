#!/bin/bash
# Installs openbis for screening for the first time. 
# openbis will be installed in the parent directory of the directory where this script is located. 

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

ROOT_DIR=$BASE/..
CONFIG=$ROOT_DIR/config
BACKUP_DIR=$ROOT_DIR/old

echo Restoring empty screening database
USER=`whoami`
psql -U postgres -c "create database $OPENBIS_DB with owner $USER template = template0 encoding = 'UNICODE'"
psql -U $USER -d $OPENBIS_DB -f $CONFIG/empty-screening-database.sql

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore*.zip

echo Installing openBIS Application Server
unzip $ROOT_DIR/openBIS-*.zip -d $ROOT_DIR
./$ROOT_DIR/openBIS-server/install.sh $ROOT_DIR/openBIS-server

$BASE/restore-config-from-backup.sh $CONFIG

mv $ROOT_DIR/*.zip $BACKUP_DIR/
$BASE/bisup.sh
sleep 20
$BASE/dssup.sh

