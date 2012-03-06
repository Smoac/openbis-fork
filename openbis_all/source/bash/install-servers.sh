#! /bin/bash
# 
# Installs openBIS AS and DSS.
# 
# Usage: install-servers.sh <servers> <config snapshot repository> <builds fetching script> <config file list 1> ... <config file list n>
# 
# 
# 
# Dependencies: 
# - servers-shutdown.sh
# - create-config-snapshot.sh
# - restore-config-snapshot.sh
# - install.sh of the openBIS AS distribution
# 
set -o nounset
set -o errexit

if [ $# -lt 4 ]; then
    echo "Usage: install-servers.sh <servers> <config snapshot repository> <builds fetching script> <config file list 1> ... <config file list n>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
BIN_DIR=`dirname "$0"`
SERVERS="$1"
REPOSITORY="$2"
FETCHING_SCRIPT="$3"
shift 3

OPENBIS_AS="$SERVERS/openBIS-server"
OPENBIS_DSS="$SERVERS/datastore_server"

"$BIN_DIR/servers-shutdown.sh" "$SERVERS"
"$BIN_DIR/create-config-snapshot.sh" "$SERVERS" "$REPOSITORY" "$@"
rm -rf "$OPENBIS_AS"
rm -rf "$OPENBIS_DSS"

"$FETCHING_SCRIPT"
echo "unzip openBIS-server*.zip"
unzip -qu openBIS-server*.zip -d "$SERVERS"
echo "unzip datastore_server-*.zip"
unzip -quo datastore_server-*.zip -d "$SERVERS" 
for file in datastore_server_plugin-*.zip; do 
	if [ -f $file ]; then 
    echo "unzip $file"
		unzip -qun -d "$SERVERS" $file;
	fi
done
rm -f openBIS-server*.zip
rm -f datastore_server*.zip

echo "==== Starting install script of the openBIS AS distribution"
"$OPENBIS_AS/install.sh" "$OPENBIS_AS"
echo "==== Finished install script of the openBIS AS distribution"

YOUNGEST_REPOSITORY=`ls "$REPOSITORY"|sort -r|sed q`
echo "==== Restore config snapshot $YOUNGEST_REPOSITORY"
"$BIN_DIR/restore-config-snapshot.sh" "$SERVERS" "$REPOSITORY/$YOUNGEST_REPOSITORY/"

