#!/bin/bash
# Upgrades openbis for screening. 
# Assumes that openbis is installed in the parent directory of the directory where this script is located. 

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

ROOT_DIR=$BASE/../servers
BACKUP_DIR=$BASE/../old

$BASE/alldown.sh

NOW=`date +%y%m%d-%H%m`

CONFIG=$BACKUP_DIR/config-backup-$NOW
mkdir -p $CONFIG
$BASE/backup-config.sh $CONFIG

OLD_BIS=$BACKUP_DIR/openBIS-server-$NOW
mv $ROOT_DIR/openBIS-server $OLD_BIS 
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server-$NOW

# pg_restore -d db-name db-file.dmp 
pg_dump -U $DB_USER_NAME -Fc $OPENBIS_DB > $BACKUP_DIR/$OPENBIS_DB-${NOW}.dmp
pg_dump -U $DB_USER_NAME -Fc $IMAGING_DB > $BACKUP_DIR/$IMAGING_DB-${NOW}.dmp

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore*.zip

echo Installing openBIS Application Server
unzip $ROOT_DIR/openBIS-*.zip -d $ROOT_DIR
$ROOT_DIR/openBIS-server/install.sh $ROOT_DIR/openBIS-server

$BASE/restore-config-from-backup.sh $CONFIG
cp -r $OLD_BIS/jetty/indices* $ROOT_DIR/openBIS-server/jetty/

mv $ROOT_DIR/*.zip $BACKUP_DIR/
$BASE/allup.sh