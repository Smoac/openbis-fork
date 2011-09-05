#! /bin/bash
# 
# This script checks whether openBIS AS server is a sprint server installed in the current week
# or not. If yes exit (with exit code 0) is executed.
# 
# usage: check-and-exit-if-new-sprint-server.sh <servers>
# 
# where 
# <servers> is the path to the directory containing the server folders 'openBIS-server' 
#    and 'datastore_server',
# 
if [ $# -ne 1 ]; then
    echo "Usage: check-and-exit-if-new-sprint-server.sh <servers>"
    exit 1
fi

SERVERS="$1"
LOG_FILE=$SERVERS/openBIS-server/jetty/logs/jetty.out

CURRENT_VERSION=UNKNOWN
if [ -f $LOG_FILE ]; then
    CURRENT_VERSION=`awk '/STATUS.CISDContextLoaderListener - Version/ {print $1" "$2" "$8" "$9}' $LOG_FILE | tail -1`
    echo "Current openBIS Application Server: $CURRENT_VERSION"
    if [ "SNAPSHOT" != "`echo $CURRENT_VERSION|awk '{print $3}'`" ]; then
        TIME_STAMP="`echo $CURRENT_VERSION|awk '{print $1, $2}'`"
        WEEK=`date --date="$TIME_STAMP" "+%W"`
        if [ $? -ne 0 ]; then
            # Mac OSX has a different flavor of date command
            WEEK=`date -j -f "%Y-%m-%d %H:%M:%S" "$TIME_STAMP" "+%W"`
        fi
        CURRENT_WEEK=`date "+%W"`
        if [ "$WEEK" = "$CURRENT_WEEK" ]; then
            echo "Sprint server not replaced until next week."
            exit
        fi
    fi 
fi
