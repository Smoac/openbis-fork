#! /bin/bash

# Status script for CISD openBIS Application Server on Unix / Linux systems
# -------------------------------------------------------------------------

source `dirname "$0"`/setup-env

printStatus()
{
  if [ "$1" == "-q" ]; then
    QUIET=1
  fi
  if [ -f $PIDFILE ]; then
    PID=`cat $JETTY_PID_FILE`
    isPIDRunning $PID
    if [ $? -eq 0 ]; then
      test -z "$QUIET" && echo "openBIS Application Server is running (pid $PID)"
      return 0
    else
      test -z "$QUIET" && echo "openBIS Application is dead (stale pid $PID)"
      return 1
    fi
  else
    test -z "$QUIET" && echo "openBIS Application is not running."
    return 2
  fi
}

printStatus "$1"