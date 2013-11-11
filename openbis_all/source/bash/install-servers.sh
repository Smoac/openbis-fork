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

if [ $# -lt 1 ]; then
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
TARBALL=`ls -1 $OPENBIS_ROOT_DIR/openBIS-installation*.tar.gz`
tar xzf "$TARBALL" -C "$OPENBIS_ROOT_DIR"
rm -rf "$TARBALL"
INSTALLER_DIR=${TARBALL%.tar.gz}
rm -f "$INSTALLER_DIR/console.properties"
cp "$OPENBIS_ROOT_DIR/console.properties" "$INSTALLER_DIR/"

echo "==== Upgrade installation"
"$INSTALLER_DIR/run-console.sh"
