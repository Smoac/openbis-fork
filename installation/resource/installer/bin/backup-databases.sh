#!/bin/bash
#
# Backups all databases controlled by openBIS to a specified folder.
#
# Usage: backup-databases.sh $BACKUP_DIR
# 

#
# Sets the variable DB_LIST to contain a list of databases 
# to be backed up.
# Each line of the function's result has the form :
#
# database=XXXX;username=XXXX;password=XXXX
#
function listDatabases() {
  
  local LIB=$DSS_SERVER/lib
  CP=`echo $LIB/datastore_server.jar $LIB/*.jar | sed 's/ /:/g'`

  DB_LIST=$(java -cp $CP ch.systemsx.cisd.openbis.dss.generic.server.dbbackup.BackupDatabaseDescriptionGenerator $@)
}

#
# Parses a property value from a semi-colon delimited string of key=value items.
# 
# $1 - a semi-color delimited string of properties (e.g. "key1=value1;key2=value2")
# $2 - the name of the property (e.g. "key1")
#
#
function getProperty() {

  local properties=$1
  local propName=$2
  echo $properties | tr ";" "\n" | grep "$propName=" | sed "s/$propName=//"
}

function checkForBackup()
{
  local database=$1
  if [ $CONSOLE ]; then
    if [ "$(trim "$DATABASES_TO_BACKUP")" == "" ]; then
      echo "TRUE"
      return
    fi
  fi
  echo $(contains "$DATABASES_TO_BACKUP" $database)
  return
}

#
# Backs up database specified by a string as returned 
# in the function listDatabases.
#
function backupDatabase() {

  DB_PROPS=$1
  PG_DUMP_OPTION=$2
  
  local database=$(getProperty $DB_PROPS "database")
  if [ $(checkForBackup $database) == "FALSE" ]; then
    return
  fi
  if [ $(databaseExist $database) == "TRUE" ]; then
    username=$(getProperty $DB_PROPS "username")
  
    local dumpDir=$BACKUP_DIR/$database
  
    echo "Backing up database $database to $dumpDir..."
    exe_pg_dump -U $username -Fd $database $PG_DUMP_OPTION -f $dumpDir
  
    if [ "$?" -ne 0 ]; then
      echo "Failed to backup database '$database' !"
      exit 3
    fi
  fi
}

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/common-functions.sh

BACKUP_DIR=$1
if [ "$BACKUP_DIR" == "" ]; then
	echo ERROR: directory in which configuration should be stored has not been specified! 
	exit 1
fi
DATABASES_TO_BACKUP="$2"
CONSOLE=$3

SERVERS=$BASE/../servers
AS_SERVER=$SERVERS/openBIS-server/
DSS_SERVER=$SERVERS/datastore_server

listDatabases $AS_SERVER/jetty/etc/service.properties $DSS_SERVER/etc/service.properties

PG_DUMP_OPTION=""
if [[ "`exe_pg_dump --version|awk '{print $3}'`" > "9.2.x" ]]; then
  if [ -f /proc/cpuinfo ]; then
    # Linux way to get number of processors
    NUMBER_OF_PROCESSORS=`grep processor /proc/cpuinfo |wc -l`
  else
    # Mac OS way to get number of processors
    NUMBER_OF_PROCESSORS=`sysctl -n hw.ncpu`
  fi
  echo Database dumping will use $NUMBER_OF_PROCESSORS processors.
  PG_DUMP_OPTION=--jobs=$NUMBER_OF_PROCESSORS
fi
for DB in $DB_LIST; do
  backupDatabase $DB $PG_DUMP_OPTION
done


