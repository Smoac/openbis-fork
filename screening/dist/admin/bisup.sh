#!/bin/bash
# Starts up openBIS server

STARTING_MESSAGE="STARTING SERVER"
STARTED_MESSAGE="SERVER STARTED"
ERROR_MESSAGE="ERROR"

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

JETTY_HOME=$BASE/../servers/openBIS-server/jetty/
OPENBIS_LOG=$JETTY_HOME/logs/openbis_log.txt

echo Starting openBIS...
echo $STARTING_MESSAGE >> $OPENBIS_LOG

$JETTY_HOME/bin/startup.sh

for i in {1..40}; do 
    sleep 1
    
    started=`egrep -R "($STARTING_MESSAGE|$STARTED_MESSAGE)" $OPENBIS_LOG | tail -1 | grep "$STARTED_MESSAGE"`
    if [ -n "$started" ]; then
        echo "Done."
        exit 0;
    fi
    
    error=`egrep -R "($STARTING_MESSAGE|$ERROR_MESSAGE)" $OPENBIS_LOG | tail -1 | grep "$ERROR_MESSAGE"`
    if [ -n "$error" ]; then
        echo "Failed: $error" 
        exit 1;
    fi
    
done


echo "Operation timed out. Waiting aborted ..."
exit 2;


