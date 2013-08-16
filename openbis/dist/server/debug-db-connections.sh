#! /bin/bash

# Script for CISD openBIS Application Server to switch on / off debugging of database connections on Unix / Linux systems
# -----------------------------------------------------------------------------------------------------------------------

source `dirname "$0"`/setup-env

printStatus -q "$1"
    EXIT_STATUS=$?
if [ $EXIT_STATUS -ne 0 ]; then
  echo "Error: Application Server not running."
  exit 100
fi

mkdir -p .control
if [ "$1" == "off" ]; then
 	touch .control/db-connections-debug-off
	echo "Switched off debug logging for database connections."
else
 	touch .control/db-connections-debug-on
	echo "Switched on debug logging for database connections."
fi
