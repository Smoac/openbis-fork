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

INSTALLER_JAR=`ps aux|grep openBIS-installer.jar|grep -v grep|awk '{for(i=1;i<=NF;i++){if($i~/openBIS-installer\.jar/){print $i}}}'`
OPENBIS_UPGRADE_VERSION_NUMBER=`java -cp $BASE InstallerVariableAccess $INSTALLER_JAR version.number`
OPENBIS_UPGRADE_REVISION_NUMBER=`java -cp $BASE InstallerVariableAccess $INSTALLER_JAR revision.number`
echo "existing version: $OPENBIS_VERSION_NUMBER:$OPENBIS_UPGRADE_NUMBER"
echo "installer jar: $INSTALLER_JAR, version: $OPENBIS_UPGRADE_VERSION_NUMBER:$OPENBIS_UPGRADE_REVISION_NUMBER"

BACKUP_DIR=$1
if [ "$BACKUP_DIR" == "" ]; then
	echo ERROR: directory in which configuration should be stored has not been specified! 
	exit 1
fi
DATABASES_TO_BACKUP="$2"
CONSOLE=$3

$BASE/alldown.sh

ROOT_DIR=$BASE/../servers
CONFIG=$BACKUP_DIR/config-backup

echo "Creating backup folder $BACKUP_DIR ..."
mkdir -p $CONFIG
$BASE/backup-config.sh $CONFIG

$BASE/backup-databases.sh $BACKUP_DIR "$DATABASES_TO_BACKUP" $CONSOLE
if [ $? -ne "0" ]; then
  echo "Creating database backups had failed. Aborting ..."
  exit 1
fi

OLD_BIS=$BACKUP_DIR/openBIS-server

echo "Moving old installation to backup dir"

if [ -d $ROOT_DIR/openBIS-server ]; then
  echo "mv $ROOT_DIR/openBIS-server $OLD_BIS"
  mv $ROOT_DIR/openBIS-server $OLD_BIS
fi
 
echo "mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server"
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server

if [ -d $ROOT_DIR/big_data_link_server ]; then
  echo "mv $ROOT_DIR/big_data_link_server $BACKUP_DIR/big_data_link_server"
  mv $ROOT_DIR/big_data_link_server $BACKUP_DIR/big_data_link_server
fi

echo "cp -R $ROOT_DIR/core-plugins $BACKUP_DIR/core-plugins"
cp -R $ROOT_DIR/core-plugins $BACKUP_DIR/core-plugins
rm -rf $BACKUP_DIR/core-plugins/eln-lims/bin

if [ -d $BACKUP_DIR/datastore_server/data/sessionWorkspace ]; then
  echo "rm -r $BACKUP_DIR/datastore_server/data/sessionWorkspace"
  rm -r $BACKUP_DIR/datastore_server/data/sessionWorkspace
fi

echo "DONE"
