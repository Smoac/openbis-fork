#! /bin/bash
# 
# Stop servers, replace store, databases and lucene index of an openBIS instance from a snapshot 
# created by create-snapshot.sh and startup servers again.
# 
# usage: servers-startup-from-snapshot.sh <servers> <snapshot file>
#
# where <servers> is the path to the directory containing the server folders 'openBIS-server' 
# and 'datastore_server'.
#
# Important Notes: 
# - The store is completely erased before restoring from snapshot.
# - Paths to store and lucene index are taken from the snapshot configuration file.
#   Currently there is no support for overriding them by command line options.
#
# Dependencies:
# - servers-shutdown.sh
# - servers-startup.sh
#
set -o nounset
set -o errexit

function getValue {
    file=$1
    key=$2
    awk -F ' *= *' -v key=$2 '{map[$1] = $2} END {print map[key]}' $file
}

function cleanUpAndExit {
    rm -rf $TMPDIR
    exit 1
}

if [ $# -ne 2 ]; then
    echo "Usage: servers-startup-from-snapshot.sh <servers> <snapshot file>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
SERVERS_PATH=$1
SNAPSHOT_FILE=$2

USER=$(whoami)
TMPDIR=`mktemp -d /tmp/snapshot-XXXXX`
echo "temp folder:$TMPDIR"
if ! tar -zxf  $SNAPSHOT_FILE -C $TMPDIR; then
    echo "Error: Couldn't unzip and untar $SNAPSHOT_FILE."
    cleanUpAndExit
fi

for f in $TMPDIR/*; do SNAPSHOT_FILES="$f"; done
SNAPSHOT_CONFIG_FILE=$SNAPSHOT_FILES/snapshot.config

OPENBIS_AS_ROOT="$SERVERS_PATH/openBIS-server/jetty/"
if [ ! -d "$OPENBIS_AS_ROOT" ]; then
    echo "Error: $OPENBIS_AS_ROOT isn't a directory."
    echo "Most probable reason: $SERVERS_PATH doesn't point to a valid openBIS instance."
    cleanUpAndExit
fi
STORE=`getValue $SNAPSHOT_CONFIG_FILE store`
DATABASES=`getValue $SNAPSHOT_CONFIG_FILE databases`
INDEX="$OPENBIS_AS_ROOT"`getValue $SNAPSHOT_CONFIG_FILE index`

##################################################
#
# Shutting down servers
#
if ! `dirname "$0"`/servers-shutdown.sh "$SERVERS_PATH"; then
    echo "Error: Couldn't shut down servers. Restoring aborted."
    cleanUpAndExit
fi

##################################################
#
# Restoring from snapshot
#
echo "==== Restore from $SNAPSHOT_FILE"
############## restore store ##############
echo "Starting to restore the store $STORE."
rm -rf "$STORE"
mkdir -p "$STORE"
if ! tar -xf $SNAPSHOT_FILES/store.tar -C "$STORE"; then
    echo "Error: Couldn't restore store. Restoring aborted."
    cleanUpAndExit
fi
echo "Store successfully restored."
############## restore databases ##############
for db in $DATABASES; do
    psql -U postgres -q -c "drop database $db"
    psql -U postgres -q -c "create database $db with owner $USER"
    if ! psql -U $USER -q -d $db -f $SNAPSHOT_FILES/$db.sql > /dev/null; then
        echo "Error: Couldn't restore database '$db'."
        cleanUpAndExit
    fi
    echo "Database '$db' has been successfully restored."
done
############## restore store ##############
rm -rf "$INDEX"
mkdir -p "$INDEX"
if ! tar -xf $SNAPSHOT_FILES/index.tar -C "$INDEX"; then
    echo "Error: Couldn't restore index."
    cleanUpAndExit
fi
echo "Index has been successfully restored."
rm -rf $TMPDIR
echo "==== Successfully restored from $SNAPSHOT_FILE"

##################################################
#
# Starting up servers
#
`dirname "$0"`/servers-startup.sh "$SERVERS_PATH"


