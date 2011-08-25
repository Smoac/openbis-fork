#! /bin/bash
# 
# Usage: install-servers.sh <servers> <config snapshot repository> <builds fetching script> <config file list 1> ... <config file list n>
# 
# 
# 
# 
# 
if [ $# -ne 4 ]; then
    echo "Usage: install-servers.sh <config snapshot repository> <builds fetching script> <config file list 1> ... <config file list n>"
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

#"$BIN_DIR/servers-shutdown.sh" "$SERVERS"
"$BIN_DIR/create-config-snapshot.sh" "$SERVERS" "$REPOSITORY" "$@"
rm -rf "$OPENBIS_AS"
rm -rf "$OPENBIS_DSS"

"$FETCHING_SCRIPT"
unzip -qu openBIS-server*.zip -d "$SERVERS"
unzip -qu datastore_server-*.zip -d "$SERVERS" 
for file in datastore_server_plugin-*.zip; do 
	if [ -f $file ]; then 
		unzip -qu -d datastore_server $file;
	fi
done
rm -f openBIS-server*.zip
rm -f datastore_server*.zip

"$OPENBIS_AS/install.sh" "$OPENBIS_AS"

YOUNGEST_REPOSITORY=`ls "$REPOSITORY"|sort -r|sed q`
"$BIN_DIR/restore-config-snapshot.sh" "$SERVERS" "$REPOSITORY/$YOUNGEST_REPOSITORY/"

