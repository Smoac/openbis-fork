#!/bin/bash
# Moves the current installation to a backup folder. 

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

BACKUP_DIR=$1
if [ "$BACKUP_DIR" == "" ]; then
	echo ERROR: directory in which configuration should be stored has not been specified! 
	exit 1
fi

source $BASE/env

$BASE/alldown.sh

ROOT_DIR=$BASE/../servers
CONFIG=$BACKUP_DIR/config-backup

echo "Creating backup folder $BACKUP_DIR ..."
mkdir -p $CONFIG
$BASE/backup-config.sh $CONFIG

OLD_BIS=$BACKUP_DIR/openBIS-server

echo "Moving old installation to backup dir"

echo "mv $ROOT_DIR/openBIS-server $OLD_BIS"
mv $ROOT_DIR/openBIS-server $OLD_BIS
 
echo "mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server"
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server



# Note: to restore the database afterwards one can use:
#    pg_restore -d db-name db-file.dmp 
echo "Creating database dumps for $OPENBIS_DB and $IMAGING_DB..."

echo "$PG_DUMP -U $DB_USER_NAME -Fc $OPENBIS_DB > $BACKUP_DIR/$OPENBIS_DB.dmp"
$PG_DUMP -U $DB_USER_NAME -Fc $OPENBIS_DB > $BACKUP_DIR/$OPENBIS_DB.dmp

echo "DONE"
