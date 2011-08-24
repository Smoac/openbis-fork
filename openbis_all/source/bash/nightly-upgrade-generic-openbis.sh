#! /bin/bash
# 
# This script does the following:
# - Install generic openBIS sprint servers based on latest builds on Hudson.
# - Restore store and databases from latest snapshot
# - Restart AS and DSS
# 

echo ":::::::::::::::::::: Nightly Upgrade Generic openBIS Servers [`date`] :::::::::::::::::::::"
LOG_FILE=sprint/openBIS-server/jetty/logs/openbis_log.txt
CURRENT_VERSION=UNKNOWN
if [ -f $LOG_FILE ]; then
    CURRENT_VERSION=`awk '/OPERATION.CISDContextLoaderListener - Version/ {print $1" "$2" "$8" "$9}' $LOG_FILE | tail -1`
fi

echo "Current openBIS Application Server: $CURRENT_VERSION"
if [ "SNAPSHOT" != "`echo $CURRENT_VERSION|awk '{print $3}'`" ]; then
    TIME_STAMP="`echo $CURRENT_VERSION|awk '{print $1, $2}'`"
    echo $TIME_STAMP
    WEEK=`date --date="$TIME_STAMP" "+%W"`
    if [ $? -ne 0 ]; then
        # Mac OSX has a different flavor of date command
        WEEK=`date -j -f "%Y-%m-%d %H:%M:%S" "$TIME_STAMP" "+%W"`
    fi
    CURRENT_WEEK=`date "+%W"`
    echo "$CURRENT_WEEK $WEEK"
    if [ "$WEEK" = "$CURRENT_WEEK" ]; then
        echo "Sprint server not replaced until next week."
        exit
    fi
fi 

fetch-ci-artifacts.sh openbis
fetch-ci-artifacts.sh rtd_yeastx
fetch-ci-artifacts.sh -p server datastore_server
#sprint_install.sh
#servers-startup-from-latest-snapshot.sh snapshots

