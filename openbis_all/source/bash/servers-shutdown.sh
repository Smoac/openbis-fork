#! /bin/bash
# 
# Shuts down openBIS AS and DSS and waits until they are stopped.
# 
# usage: servers-shutdown.sh <servers folder>
#
# Important Note: 
# - It is assumed that the following folders are the working directories of AS and DSS, respectively:
#   <servers folder>/openBIS-server/jetty
#   <servers folder>/datastore_server
#   If one of these folders do not exists shutdown is aborted.
#
set -o nounset
set -o errexit

if [ $# -ne 1 ]; then
    echo "Usage: servers-shutdown.sh <servers folder>"
    exit 1
fi

SERVERS_FOLDER="$1"

AS="$SERVERS_FOLDER/openBIS-server/jetty"
if [ ! -d "$AS" ]; then
    echo "$AS doesn't exist or isn't a directory."
    exit 1
fi
LOG_FILE="$AS/logs/openbis_log.txt"
DSS="$SERVERS_FOLDER/datastore_server"
if [ ! -d "$DSS" ]; then
    echo "$DSS doesn't exist or isn't a directory."
    exit 1
fi

# Shutdown DSS and AS, ignoring exit codes != 0
set +o errexit
"$DSS/datastore_server.sh" stop
"$AS/bin/shutdown.sh"
set -o errexit

if [ ! -f "$LOG_FILE" ]; then
    exit
fi
STARTING_MESSAGE="STARTING SERVER"
STOPPED_MESSAGE="SERVER STOPPED"
for i in {1..10}; do 
    echo -n "."
    sleep 2
    
    set +o errexit
    started=`egrep -R "($STARTING_MESSAGE|$STOPPED_MESSAGE)" "$LOG_FILE" | tail -1 | grep "$STOPPED_MESSAGE"`
    set -o errexit
    if [ -n "$started" ]; then
        echo "Done."
        exit 0;
    fi
done

echo "Operation timed out."
