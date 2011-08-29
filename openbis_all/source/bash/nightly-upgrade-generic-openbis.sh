#! /bin/bash
# 
# This script does the following:
# - Creates a config snapshot of current installation.
# - Install generic openBIS sprint servers based on latest builds on Hudson.
# - Using previous config files.
# - Restore store and databases from latest snapshot.
# - Restart AS and DSS.
# 
# Crontab entry:
# 30 3  *  *  *  [ -f $HOME/.profile ] && . $HOME/.profile && [ -f $HOME/.bash_profile ] && . $HOME/.bash_profile && nightly-upgrade-generic-openbis.sh &>> openbis-cronjob.txt
# 

BIN_DIR=`dirname "$0"`
SERVERS=sprint

echo ":::::::::::::::::::: Nightly Upgrade Generic openBIS Servers [`date`] :::::::::::::::::::::"

##################################################
#
# Check whether upgrade should be done or not
#
LOG_FILE=$SERVERS/openBIS-server/jetty/logs/openbis_log.txt
CURRENT_VERSION=UNKNOWN
if [ -f $LOG_FILE ]; then
    CURRENT_VERSION=`awk '/OPERATION.CISDContextLoaderListener - Version/ {print $1" "$2" "$8" "$9}' $LOG_FILE | tail -1`
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

##################################################
#
# Upgrade servers and restart them
#

"$BIN_DIR/install-servers.sh" "$SERVERS"/ config-snapshots/ "$BIN_DIR/fetch-generic-sprint-server-artifacts.sh" "$BIN_DIR/config-files.txt" 
"$BIN_DIR/servers-startup-from-latest-snapshot.sh" "$SERVERS" snapshots

