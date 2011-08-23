#! /bin/bash
# 
# Stop servers, replace store, databases and lucene index of an openBIS instance from latest snapshot 
# created by create-snapshot.sh and startup servers again.
# 
# usage: servers-startup-from-latest-snapshot.sh <snapshot repository>
#
# Important Notes: 
# - This script should be run after all servers have been stopped.
# - The store is completely erased before restoring from snapshot.
# - Paths to store and lucene index are taken from the snapshot configuration file.
#   Currently there is no support for overriding them by command line options.
#
# Dependencies:
# - restore-from-snapshot.sh
#
if [ $# -ne 1 ]; then
    echo "Usage: servers-startup-from-latest-snapshot.sh <snapshot repository>"
    exit 1
fi
SNAPSHOT_REPOSITORY=$1
if [ ! -d "$SNAPSHOT_REPOSITORY" ]; then
    echo "$SNAPSHOT_REPOSITORY doesn't exist or isn't a directory."
    exit 1
fi
`dirname "$0"`/servers-startup-from-snapshot.sh "$SNAPSHOT_REPOSITORY"/`ls "$SNAPSHOT_REPOSITORY"|sort -r|sed q`
