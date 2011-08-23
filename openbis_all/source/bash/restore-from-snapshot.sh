#! /bin/bash
# 
# Restores store, databases and optionally lucene index of an openBIS instance from a snapshot 
# created by create-snapshot.sh.
# 
# usage: restore-from-snapshot.sh <snapshot file>
#
function getValue {
    file=$1
    key=$2
    awk -F ' *= *' -v key=$2 '{map[$1] = $2} END {print map[key]}' $file
}

function cleanUpAndExit {
    rm -rf $TMPDIR
    exit 1
}

if [ $# -ne 1 ]; then
    echo "Usage: restore-from-snapshot.sh <snapshot file>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
SNAPSHOT_FILE=$1

USER=$(whoami)
TMPDIR=`mktemp -d /tmp/snapshot-XXXXX`
echo "temp folder:$TMPDIR"
tar -zxf  $SNAPSHOT_FILE -C $TMPDIR
if [ $? -ne 0 ]; then
    echo "Error: Couldn't unzip and untar $SNAPSHOT_FILE."
    cleanUpAndExit
fi

for f in $TMPDIR/*; do SNAPSHOT_FILES="$f"; done
SNAPSHOT_CONFIG_FILE=$SNAPSHOT_FILES/snapshot.config

SERVERS_PATH=`getValue $SNAPSHOT_CONFIG_FILE servers`
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
# Restoring from snapshot
#
echo "==== Restore from $SNAPSHOT_FILE"
############## restore store ##############
echo "Starting to restore the store $STORE."
rm -rf "$STORE"
mkdir -p "$STORE"
tar -xf $SNAPSHOT_FILES/store.tar -C "$STORE"
if [ $? -ne 0 ]; then
    echo "Error: Couldn't restore store. Restoring aborted."
    cleanUpAndExit
fi
echo "Store successfully restored."
############## restore databases ##############
for db in $DATABASES; do
    psql -U postgres -q -c "drop database $db"
    psql -U postgres -q -c "create database $db with owner $USER"
    psql -U $USER -q -d $db -f $SNAPSHOT_FILES/$db.sql > /dev/null
    if [ $? -ne 0 ]; then
        echo "Error: Couldn't restore database '$db'."
        cleanUpAndExit
    fi
    echo "Database '$db' has been successfully restored."
done
############## restore store ##############
if [ -n "$INDEX" ]; then
    tar -xf $SNAPSHOT_FILES/index.tar -C "$INDEX"
    if [ $? -ne 0 ]; then
        echo "Error: Couldn't restore index."
        cleanUpAndExit
    fi
    echo "Index has been successfully restored."
fi
rm -rf $TMPDIR
echo "==== Successfully restored from $SNAPSHOT_FILE"

