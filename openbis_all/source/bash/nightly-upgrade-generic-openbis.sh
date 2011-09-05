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
# Dependencies:
# - check-and-exit-if-new-sprint-server.sh
# - install-servers.sh
# - fetch-generic-sprint-server-artifacts.sh
# - servers-startup-from-latest-snapshot.sh
# - config-files.txt
#

BIN_DIR=`dirname "$0"`
SERVERS=sprint

echo ":::::::::::::::::::: Nightly Upgrade Generic openBIS Servers [`date`] :::::::::::::::::::::"

"$BIN_DIR/check-and-exit-if-new-sprint-server.sh" "$SERVERS"

##################################################
#
# Upgrade servers and restart them
#

"$BIN_DIR/install-servers.sh" "$SERVERS"/ config-snapshots/ "$BIN_DIR/fetch-generic-sprint-server-artifacts.sh" "$BIN_DIR/config-files.txt" 
"$BIN_DIR/servers-startup-from-latest-snapshot.sh" "$SERVERS" snapshots

