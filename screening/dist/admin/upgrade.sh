#!/bin/bash
# Upgrades openbis AS and DSS. 
# Assumes that openbis is installed in the 'servers' directory on the same level as this script parent directory

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env

ROOT_DIR=$BASE/../servers

NOW=`date +%y%m%d-%H%m`
BACKUP_DIR=$BASE/../backup/$NOW

$BASE/alldown.sh


CONFIG=$BACKUP_DIR/config-backup
mkdir -p $CONFIG
$BASE/backup-config.sh $CONFIG

OLD_BIS=$BACKUP_DIR/openBIS-server
mv $ROOT_DIR/openBIS-server $OLD_BIS 
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server

# Note: to restore the database afterwards one can use:
#    pg_restore -d db-name db-file.dmp 
pg_dump -U $DB_USER_NAME -Fc $OPENBIS_DB > $BACKUP_DIR/$OPENBIS_DB-${NOW}.dmp
# screening-specific
pg_dump -U $DB_USER_NAME -Fc $IMAGING_DB > $BACKUP_DIR/$IMAGING_DB-${NOW}.dmp

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore_server-screening*.zip -d $ROOT_DIR
unzip $ROOT_DIR/datastore_server_plugin*.zip -d $ROOT_DIR

echo Installing openBIS Application Server
unzip $ROOT_DIR/openBIS-*.zip -d $ROOT_DIR
$ROOT_DIR/openBIS-server/install.sh $ROOT_DIR/openBIS-server

$BASE/restore-config-from-backup.sh $CONFIG
cp -r $OLD_BIS/jetty/indices* $ROOT_DIR/openBIS-server/jetty/

mv $ROOT_DIR/*.zip $BACKUP_DIR/
$BASE/allup.sh