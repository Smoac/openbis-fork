#!/bin/bash
# Starts up openBIS server

#
# Return the age of a file in seconds.
#
# parameter $1: a file name
#
function fileAgeInSeconds() {

  local fileName=$1
  
  # Linux-style stat command
  fftime=$(stat -c "%Y" $fileName 2> /dev/null )
  
  if [ $? -ne 0 ]; then
     # error, perhaps we run on FreeBSD/MacOS ?
     fftime=$(stat -f "%m" $fileName)
  fi
  
  nnow=$(date +%s)

  return $(expr $nnow - $fftime)
}

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
TIMEOUT=15

echo Starting openBIS...
echo $STARTING_MESSAGE >> $OPENBIS_LOG

$JETTY_HOME/bin/startup.sh

bisLogAgeInSeconds=5
jettyLogAgeInSeconds=5

# 
# Loop while the openBIS process alters writes to the log files
#
while [ "$bisLogAgeInSeconds" -lt $TIMEOUT ] || [ "$jettyLogAgeInSeconds" -lt $TIMEOUT ]; do

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
    
    fileAgeInSeconds $OPENBIS_LOG
    bisLogAgeInSeconds=$?
    
    fileAgeInSeconds $JETTY_LOG
    jettyLogAgeInSeconds=$?
done


echo "Operation timed out. Waiting aborted ..."
exit 2;


