#!/bin/bash
# Starts up openBIS server

STARTING_MESSAGE="STARTING SERVER"
STARTED_MESSAGE="SERVER STARTED"
ERROR_MESSAGE="ERROR"

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


JETTY_HOME=$BASE/../servers/openBIS-server/jetty
OPENBIS_LOG=$JETTY_HOME/logs/openbis_log.txt
JETTY_LOG=$JETTY_HOME/logs/jetty.out

echo Starting openBIS...
echo $STARTING_MESSAGE >> $OPENBIS_LOG

$JETTY_HOME/bin/startup.sh

for i in {1..120}; do 
    echo -n "."
    sleep 2
    
    started=`egrep -R "($STARTING_MESSAGE|$STARTED_MESSAGE)" $OPENBIS_LOG | tail -1 | grep "$STARTED_MESSAGE"`
    if [ -n "$started" ]; then
        started=`egrep -R "($STARTING_MESSAGE|$STARTED_MESSAGE)" $JETTY_LOG | tail -1 | grep "$STARTED_MESSAGE"`
        if [ -n "$started" ]; then
            echo "Done."
            exit 0;
        fi
    fi
    
    error=`egrep -R "($STARTING_MESSAGE|$ERROR_MESSAGE)" $OPENBIS_LOG | tail -1 | grep "$ERROR_MESSAGE"`
    if [ -n "$error" ]; then
        echo "Failed: $error" 
        exit 1;
    fi
    
    error=`egrep -R "($STARTING_MESSAGE|$ERROR_MESSAGE)" $JETTY_LOG | tail -1 | grep "$ERROR_MESSAGE"`
    if [ -n "$error" ]; then
        echo "Failed: $error" 
        exit 1;
    fi
    
done


echo "Operation timed out. Waiting aborted ..."
exit 2;


