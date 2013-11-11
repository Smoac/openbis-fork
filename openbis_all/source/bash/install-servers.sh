#! /bin/bash
# 
# Installs openBIS AS and DSS.
# 
# Usage: install-servers.sh <openBIS installation folder>
# 
# 
# 
# Dependencies: 
# - fetch-ci-artifacts.sh
# 
set -o nounset
set -o errexit

if [ $# -lt 4 ]; then
    echo "Usage: install-servers.sh <openBIS installation folder>"
    exit 1
fi

##################################################
#
# Gathering parameters
#
BIN_DIR=`dirname "$0"`
OPENBIS_ROOT_DIR="$1"

echo "==== Fetsching openBIS installation tar ball"
rm -rf "$OPENBIS_ROOT_DIR/"openBIS-installation*tar.gz
"$BIN_DIR/fetch-ci-artifacts.sh" -d "$OPENBIS_ROOT_DIR" gradle-installation

echo "==== Unpack installation tar ball and replace console.properties"
tar xzf "$OPENBIS_ROOT_DIR/"openBIS-installation*tar.gz -C "$OPENBIS_ROOT_DIR"
rm -rf "$OPENBIS_ROOT_DIR/"openBIS-installation*tar.gz
rm -f "$OPENBIS_ROOT_DIR/"openBis-installation*/console.properties
cp "$OPENBIS_ROOT_DIR/console.properties" "$OPENBIS_ROOT_DIR/"openBis-installation*/

echo "==== Upgrade installation"
"$OPENBIS_ROOT_DIR/"openBis-installation*/run-console.sh
