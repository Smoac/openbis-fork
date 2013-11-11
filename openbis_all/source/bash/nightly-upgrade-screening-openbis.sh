#! /bin/bash
# 
# This script does the following:
# - Creates a config snapshot of current screening installation.
# - Install screening openBIS sprint servers based on latest builds on Hudson/Jenkins.
# - Using previous config files.
# - Restore store and databases from latest snapshot.
# - Restart AS and DSS.
# 
# Crontab entry:
# 30 3  *  *  *  [ -f $HOME/.profile ] && . $HOME/.profile && [ -f $HOME/.bash_profile ] && . $HOME/.bash_profile && nightly-upgrade-screening-openbis.sh &>> screening/openbis-cronjob.txt
# 
# Dependencies:
# - check-and-exit-if-new-sprint-server.sh
# - install-servers.sh
# - servers-startup-from-latest-snapshot.sh
#
set -o nounset
set -o errexit

BIN_DIR=`dirname "$0"`
BASE_DIR=screening
SERVERS=$BASE_DIR/servers
VERSION_FILE=$BASE_DIR/sprint-versions.txt

echo ":::::::::::::::::::: Nightly Upgrade Screening openBIS Servers [`date`] :::::::::::::::::::::"

if ! "$BIN_DIR/check-and-exit-if-new-sprint-server.sh" "$SERVERS" "$VERSION_FILE"; then exit; fi

##################################################
#
# Upgrade servers and restart them
#

"$BIN_DIR/install-servers.sh" $BASE_DIR 
"$BIN_DIR/servers-startup-from-latest-snapshot.sh" "$SERVERS" $BASE_DIR/snapshots

