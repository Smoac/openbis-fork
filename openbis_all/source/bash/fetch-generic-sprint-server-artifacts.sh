#! /bin/bash
# 
# Fetches from CI server all artifacts needed for a generic openBIS installation.
#
# Dependencies:
# - fetch-ci-artifacts.sh
#

BIN_DIR=`dirname "$0"`
SCRIPT="$BIN_DIR/fetch-ci-artifacts.sh"

"$SCRIPT" openbis
"$SCRIPT" rtd_yeastx
"$SCRIPT" -p server datastore_server
