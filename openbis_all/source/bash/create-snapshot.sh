#! /bin/bash
# 
# Creates a snapshot of an openBIS instance based on a configuration file.
# 
# usage: create-snapshot.sh <servers> <snapshot repository> <configuration file>
# 
# where 
# <servers> is the path to the directory containing the server folders 'openBIS-server' 
#    and 'datastore_server',
# <snapshot repository> is the path to the directory which will store the snapshot,
# <configuration file> is a file with the following key-value pairs (stored as <key> = <value>):
#    store        Absolute path to the directory which contains the data set store. Everything there
#                 will be added to the snapshot.
#    databases    Space separated list of databases to be dumped.
#    index        Relative path to the lucene index. 
#                 It is relative to <servers>/openBIS-server/jetty/. 
# 
# Important Notes: 
# - In order to get a consistent snapshot this script should be run after all servers have been stopped.
# - Store dump doesn't contain archived data sets and data sets in a share which is a symbolic link. 
# - The configuration file of the argument is stored in the snapshot. It is used for restoring.
# 
set -o nounset
set -o errexit

function getValue {
    file=$1
    key=$2
    awk -F ' *= *' -v key=$2 '{map[$1] = $2} END {print map[key]}' $file
}

if [ $# -ne 3 ]; then
    echo "Usage: create-snapshot.sh <servers> <snapshot repository> <configuration file>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
SERVERS_PATH="$1"
REPOSITORY="$2"
CONFIGURATION_FILE="$3"

OPENBIS_AS_ROOT="$SERVERS_PATH/openBIS-server/jetty/"
if [ ! -d "$OPENBIS_AS_ROOT" ]; then
    echo "Error: $OPENBIS_AS_ROOT isn't a directory."
    echo "Most probable reason: $SERVERS_PATH doesn't point to a valid openBIS instance."
    exit 1
fi
STORE=`getValue $CONFIGURATION_FILE store`
if [ -z "$STORE" ]; then
    echo "Store not specified in $CONFIGURATION_FILE."
    exit 1
fi
if [ ! -d "$STORE" ]; then
    echo "Store path $STORE doesn't point to an existing directory."
    exit 1
fi
DATABASES=`getValue $CONFIGURATION_FILE databases`
if [ -z "$DATABASES" ]; then
    echo "At least one database has to be specified in $CONFIGURATION_FILE."
    exit 1
fi
TIMESTAMP=`date +%Y-%m-%d_%H%M%S`
SNAPSHOT_FOLDER_NAME="openbis-snapshot-$TIMESTAMP"
SNAPSHOT="$REPOSITORY/$SNAPSHOT_FOLDER_NAME"

##################################################
#
# Creating snapshot
#
echo "==== Creating snapshot $SNAPSHOT.tgz"

mkdir -p "$SNAPSHOT"
cp -p "$CONFIGURATION_FILE" "$SNAPSHOT/snapshot.config"
############## dump the store ##############
for path in "$STORE"/*; do
    index_of_last_slash=`expr $path : '.*/'`
    file_name=${path:$index_of_last_slash}
    if [ `expr $file_name : '[0-9]*'` -ne 0 ]; then 
        if [ -h "$path" ]; then
            echo "Share $file_name is not dumped because it is a symbolic link."
        else
            echo "Start dumping share $file_name."
            parent_folder=${path:0:$index_of_last_slash}
            if ! tar -rf "$SNAPSHOT/store.tar" -C "$parent_folder" $file_name; then
                echo "Error while dumping share $file_name. Snapshot creation aborted."
                exit 1
            fi
            echo "Share $file_name sucessfully dumped."
        fi
    fi
done
echo "Dump of store $STORE has been successfully created."
############## dump databases ##############
for db in $DATABASES; do
    if ! pg_dump -U postgres -O $db > "$SNAPSHOT/$db.sql"; then
        echo "Error dumping database '$db'. Snapshot creation aborted."
        exit 1
    fi
    echo "Database '$db' has been successfully dumped."
done
############## packaging ##############
if ! tar -zcf "$SNAPSHOT.tgz" -C "$REPOSITORY" "$SNAPSHOT_FOLDER_NAME"; then
    echo "Error packaging snapshot $SNAPSHOT."
    exit 1
fi
rm -rf "$SNAPSHOT"

echo "==== $SNAPSHOT.tgz successfully created."
    



