#! /bin/bash

# Script for CISD openBIS Application Server to log open database connections on Unix / Linux systems
# ---------------------------------------------------------------------------------------------------

source `dirname "$0"`/setup-env

printStatus -q "$1"
    EXIT_STATUS=$?
if [ $EXIT_STATUS -ne 0 ]; then
  echo "Error: Application Server not running."
  exit 100
fi

mkdir -p .control
if [ "$1" != "" ]; then
 	touch .control/db-connections-print-active-$1
else
 	touch .control/db-connections-print-active
fi
echo "Active database connections logged to logs/jetty.out"
