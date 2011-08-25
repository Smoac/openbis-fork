#! /bin/bash
# 
# Stop servers, replace store, databases and lucene index of an openBIS instance from latest snapshot 
# created by create-snapshot.sh and startup servers again.
# 
# usage: servers-startup-from-latest-snapshot.sh <servers> <snapshot repository>
#
# Important Notes: 
# - This script should be run after all servers have been stopped.
# - The store is completely erased before restoring from snapshot.
# - Paths to store and lucene index are taken from the snapshot configuration file.
#   Currently there is no support for overriding them by command line options.
#
# Dependencies:
# - servers-startup-from-snapshot.sh
#

if [ $# -ne 2 ]; then
    echo "Usage: servers-startup-from-latest-snapshot.sh <servers> <snapshot repository>"
    exit 1
fi

BIN_DIR=`dirname "$0"`
SERVERS="$1"
SNAPSHOT_REPOSITORY="$2"

if [ ! -d "$SNAPSHOT_REPOSITORY" ]; then
    echo "$SNAPSHOT_REPOSITORY doesn't exist or isn't a directory."
    exit 1
fi
LATEST_SNAPSHOT=`ls "$SNAPSHOT_REPOSITORY/openbis-snapshot"*|sort -r|sed q`
"$BIN_DIR/servers-startup-from-snapshot.sh" "$SERVERS" "$SNAPSHOT_REPOSITORY/$LATEST_SNAPSHOT"
