#! /bin/bash
# 
# Fetches from CI server all artifacts needed for a screening openBIS installation.
#
# Dependencies:
# - fetch-ci-artifacts.sh
#
set -o nounset
set -o errexit

BIN_DIR=`dirname "$0"`
SCRIPT="$BIN_DIR/fetch-ci-artifacts.sh"

"$SCRIPT" -p server-screening screening
