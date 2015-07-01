#! /bin/bash
# 
# This script checks whether openBIS AS server is a sprint server installed in the current week
# or not. If yes it exits with exit code 1.
# 
# usage: check-and-exit-if-new-sprint-server.sh <servers> <version file>
# 
# where 
# <servers> is the path to the directory containing the server folders 'openBIS-server' 
#    and 'datastore_server',
# <version file> is a file which will contain the time stamp of first installation of 
#    a major version. If it doesn't exist it will be created.
# 
# 
set -o nounset
set -o errexit

function getValue {
    file="$1"
    if [ -f "$file" ]; then
        awk -F ' *= *' -v key=$2 '{map[$1] = $2} END {print map[key]}' "$file"
    fi
}

SERVERS="$1"
VERSION_FILE="$2"
LOG_FILE=`ls -t $SERVERS/openBIS-server/jetty/logs/openbis_log.txt*|tail -n 1`

CURRENT_VERSION=UNKNOWN
if [ -f $LOG_FILE ]; then
    CURRENT_VERSION=`awk '/STATUS.CISDContextLoaderListener - Version/ {print $1" "$2" "$8" "$9}' $LOG_FILE | head -n 1`
    CURRENT_VERSION_NAME=`echo $CURRENT_VERSION|awk '{print $3}'`
    if [ "$CURRENT_VERSION_NAME" == "" ]; then
        echo "Current version of openBIS Application Server not known"
        exit
    fi
    echo "Current openBIS Application Server: $CURRENT_VERSION"
    if [ "SNAPSHOT" != "$CURRENT_VERSION_NAME" ]; then
        CURRENT_MAJOR_VERSION=${CURRENT_VERSION_NAME%.*}
        echo "Current major version: $CURRENT_MAJOR_VERSION"
        TIME_STAMP=`getValue "$VERSION_FILE" $CURRENT_MAJOR_VERSION`
        if [ -z "$TIME_STAMP" ]; then 
            TIME_STAMP="`echo $CURRENT_VERSION|awk '{print $1, $2}'`"
            echo "$CURRENT_MAJOR_VERSION = $TIME_STAMP" >> "$VERSION_FILE"
        fi
        echo "Time stamp of major version: $TIME_STAMP"
        if ! WEEK=`date --date="$TIME_STAMP" "+%W"`; then
            # Mac OSX has a different flavor of date command
            WEEK=`date -j -f "%Y-%m-%d %H:%M:%S" "$TIME_STAMP" "+%W"`
        fi
        CURRENT_WEEK=`date "+%W"`
        if [ "$WEEK" = "$CURRENT_WEEK" ]; then
            echo "Sprint server not replaced until next week."
            exit 1
        fi
    fi 
fi
