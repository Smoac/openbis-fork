#!/bin/bash
# Installs openbis AS and DSS for the first time. 
# openBIS will be installed in the 'servers' directory on the same level as this script parent directory.

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

ROOT_DIR=$BASE/../servers
BACKUP_DIR=$BASE/../backup

mkdir -p $ROOT_DIR
mkdir -p $BACKUP_DIR

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore*.zip -d $ROOT_DIR

echo Installing openBIS Application Server
unzip $ROOT_DIR/openBIS-*.zip -d $ROOT_DIR
$ROOT_DIR/openBIS-server/install.sh $ROOT_DIR/openBIS-server

mv $ROOT_DIR/*.zip $BACKUP_DIR/


