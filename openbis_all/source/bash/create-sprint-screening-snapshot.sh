#! /bin/bash
#
# Creates a snapshot of the sprint screening servers assuming standard paths to servers, 
# snapshot repository and configuration file.
#
# Usage: create-sprint-screening-snapshot.sh
#
# Important Notes: see Important Notes of create-snapshot.sh 
#
# Dependencies:
# - create-snapshot.sh
#
set -o nounset
set -o errexit

BIN_DIR=`dirname "$0"`
REPOSITORY=~/screening/snapshots

$BIN_DIR/create-snapshot.sh ~/screening/servers $REPOSITORY $REPOSITORY/snapshot.config