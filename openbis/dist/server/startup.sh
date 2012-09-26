#! /bin/bash

# Startup script for CISD openBIS Application Server on Unix / Linux systems
# -------------------------------------------------------------------------

source `dirname "$0"`/setup-env

checkNotRoot

bin/status.sh -q
if [ $? -eq 0 ]; then
  echo openBIS AS already running, shut it down before starting a new one. > /dev/stderr
  exit 1
fi

$JVM -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     $JAVA_OPTS $JAVA_MEM_OPTS \
     -Dpython.path=$JETTY_LIB_PATH \
     -jar start.jar etc/jetty.xml lib=webapps/openbis/WEB-INF/lib >> logs/jetty.out 2>&1 &

# Write PID to PID file
echo $! > "$JETTY_PID_FILE"     
