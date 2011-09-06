#! /bin/bash
# 
# Starts openBIS AS and DSS and waits until they are started.
# 
# usage: servers-startup.sh <servers folder>
#
# Important Note: 
# - It is assumed that the following folders are the working directories of AS and DSS, respectively:
#   <servers folder>/openBIS-server/jetty
#   <servers folder>/datastore_server
#   If one of these folders do not exists there will be no start up.
#
set -o nounset
set -o errexit

if [ $# -ne 1 ]; then
    echo "Usage: servers-startup.sh <servers folder>"
    exit 1
fi

SERVERS_FOLDER="$1"

AS="$SERVERS_FOLDER/openBIS-server/jetty"
if [ ! -d "$AS" ]; then
    echo "$AS doesn't exist or isn't a directory."
    exit 1
fi
DSS="$SERVERS_FOLDER/datastore_server"
if [ ! -d "$DSS" ]; then
    echo "$DSS doesn't exist or isn't a directory."
    exit 1
fi


OPENBIS_LOG="$AS/logs/openbis_log.txt"
JETTY_LOG="$AS/logs/jetty.out"
STARTING_MESSAGE="STARTING SERVER"
STARTED_MESSAGE="SERVER STARTED"
ERROR_MESSAGE="ERROR"

echo -n "Starting openBIS "
echo $STARTING_MESSAGE >> $OPENBIS_LOG
"$AS/bin/startup.sh"
EXIT_CODE=2
for i in {1..120}; do 
    echo -n "."
    sleep 2
    
    started=`egrep -R "($STARTING_MESSAGE|$STARTED_MESSAGE)" $OPENBIS_LOG | tail -1 | grep "$STARTED_MESSAGE"`
    if [ -n "$started" ]; then
        started=`egrep -R "($STARTING_MESSAGE|$STARTED_MESSAGE)" $JETTY_LOG | tail -1 | grep "$STARTED_MESSAGE"`
        if [ -n "$started" ]; then
            echo "Done."
            EXIT_CODE=0
            break
        fi
    fi
    
    error=`egrep -R "($STARTING_MESSAGE|$ERROR_MESSAGE)" $OPENBIS_LOG | tail -1 | grep "$ERROR_MESSAGE"`
    if [ -n "$error" ]; then
        echo "Failed: $error" 
        EXIT_CODE=1
        break
    fi
    
    error=`egrep -R "($STARTING_MESSAGE|$ERROR_MESSAGE)" $JETTY_LOG | tail -1 | grep "$ERROR_MESSAGE"`
    if [ -n "$error" ]; then
        echo "Failed: $error" 
        EXIT_CODE=1
        break
    fi
done

if [ $EXIT_CODE -eq 2 ]; then
    echo "Operation timed out. Waiting aborted."
fi
if [ $EXIT_CODE -ne 0 ]; then
    exit $EXIT_CODE
fi

"$DSS/datastore_server.sh" start
