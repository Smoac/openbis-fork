#! /bin/bash

# Shutdown script for CISD openBIS Application Server on Unix / Linux systems
# -------------------------------------------------------------------------

source `dirname "$0"`/setup-env

$JVM -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     $JAVA_OPTS $JAVA_MEM_OPTS \
     -jar start.jar --stop

# Delete PID file
test -f "$JETTY_PID_FILE" && rm -f "$JETTY_PID_FILE"