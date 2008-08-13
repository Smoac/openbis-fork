#!/bin/bash
#
# Control script for CISD openBIS Data Set Download Server on Unix / Linux systems

awkBin()
{
  # We need a awk that accepts variable assignments with '-v'
  case `uname -s` in
    "SunOS")
      echo "nawk"
      return
      ;;
  esac
  # default
  echo "awk"
}

isPIDRunning()
{
  if [ "$1" = "" ]; then
    return 1
  fi
  if [ "$1" = "fake" ]; then # for unit tests
    return 0
  fi
  # This will have a return value of 0 on BSDish systems
  isBSD="`ps aux > /dev/null 2>&1; echo $?`"
  AWK=`awkBin`
  if [ "$isBSD" = "0" ]; then
    if [ "`ps aux | $AWK -v PID=$1 '{if ($2==PID) {print "FOUND"}}'`" = "FOUND" ]; then
      return 0
    else
      return 1
    fi
  else
    if [ "`ps -ef | $AWK -v PID=$1 '{if ($2==PID) {print "FOUND"}}'`" = "FOUND" ]; then
      return 0
    else
      return 1
    fi
  fi
}

rotateLogFiles()
{
  logfile=$1
  max=$2
  if [ -z "$logfile" ]; then
    echo "Error: rotateLogFiles: logfile argument missing"
    return 1
  fi
  if [ -z "$max" ]; then
    echo "Error: rotateLogFiles: max argument missing"
    return 1
  fi
  test -f $logfile.$max && rm $logfile.$max
  n=$max
  while [ $n -gt 1 ]; do
    nnew=$(($n-1))
    test -f $logfile.$nnew && mv $logfile.$nnew $logfile.$n
    n=$nnew
  done
  test -f $logfile && mv $logfile $logfile.1
}

getStatus()
{
  if [ -f $PIDFILE ]; then
    PID=`cat $PIDFILE`
    isPIDRunning $PID
    if [ $? -eq 0 ]; then
      return 0
    else
      return 1
    fi
  else
    return 2
  fi
}
			
printStatus()
{
  if [ -f $PIDFILE ]; then
    PID=`cat $PIDFILE`
    isPIDRunning $PID
    if [ $? -eq 0 ]; then
      echo "Data Set Download Server is running (pid $PID)"
      return 0
    else
      echo "Data Set Download Server is dead (stale pid $PID)"
      return 1
    fi
  else
    echo "Data Set Download Server is not running."
    return 2
  fi
}

#
# definitions
#

PIDFILE=${DOWNLOAD_SERVICE_PID:-download_service.pid}
CONFFILE=etc/dataset_download.conf
LOGFILE=log/dataset_download_log.txt
STARTUPLOG=log/startup_log.txt
SUCCESS_MSG="Data set download server ready"
JAR_FILE=lib/download-server.jar
MAX_LOOPS=10

#
# change to installation directory
#
bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD=`dirname $bin`
cd $WD
SCRIPT=./`basename $0`

#
# source configuration script, if any
#
test -f $CONFFILE && source $CONFFILE
if [ "$JAVA_HOME" != "" ]; then
	JAVA_BIN="$JAVA_HOME/bin/java"
else
	JAVA_BIN="java"
fi

command=$1
ALL_JAVA_OPTS="-Djavax.net.ssl.trustStore=etc/openBIS.keystore $JAVA_OPTS"
# ensure that we ignore a possible prefix "--" for any command 
command="${command#--*}"
case "$command" in
  start)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -eq 0 ]; then
      echo "Cannot start Data Set Download Server: already running."
      exit 100
    fi

    echo -n "Starting Data Set Download Server "
    rotateLogFiles $LOGFILE $MAXLOGS
    shift 1
    ${JAVA_BIN} ${ALL_JAVA_OPTS} -jar $JAR_FILE "$@" > $STARTUPLOG 2>&1 & echo $! > $PIDFILE
    if [ $? -eq 0 ]; then
      # wait for initial self-test to finish
      n=0
      while [ $n -lt $MAX_LOOPS ]; do
        sleep 1
        if [ ! -f $PIDFILE ]; then
          break
        fi
        if [ -s $STARTUPLOG ]; then
          PID=`cat $PIDFILE 2> /dev/null`
          isPIDRunning $PID
          if [ $? -ne 0 ]; then
            break
          fi
        fi
        grep "$SUCCESS_MSG" $LOGFILE > /dev/null 2>&1
        if [ $? -eq 0 ]; then
          break
        fi
        n=$(($n+1))
      done 
      PID=`cat $PIDFILE 2> /dev/null`
      isPIDRunning $PID
      if [ $? -eq 0 ]; then
        grep "$SUCCESS_MSG" $LOGFILE > /dev/null 2>&1
        if [ $? -ne 0 ]; then
          echo "(pid $PID - WARNING: SelfTest not yet finished)"
        else
          echo "(pid $PID)"
        fi
      else
        echo "FAILED"
        echo "startup log says:"
        cat $STARTUPLOG
      fi
    else
      echo "FAILED"
    fi
		;;
  stop)
    echo -n "Stopping Data Set Download Server "
    if [ -f $PIDFILE ]; then
      PID=`cat $PIDFILE 2> /dev/null`
      isPIDRunning $PID
      if [ $? -eq 0 ]; then
        kill $PID
        n=0
        while [ $n -lt $MAX_LOOPS ]; do
          isPIDRunning $PID
          if [ $? -ne 0 ]; then
            break
          fi
          sleep 1
          n=$(($n+1))
        done
        isPIDRunning $PID
        if [ $? -ne 0 ]; then
          echo "(pid $PID)"
          test -f $PIDFILE && rm $PIDFILE 2> /dev/null
        else
          echo "FAILED"
        fi
      else
        if [ -f $PIDFILE ]; then
          rm $PIDFILE 2> /dev/null
          echo "(was dead - cleaned up pid file)"
        fi
      fi
    else
      echo "(not running - nothing to do)"
    fi
    ;;
  status)
    printStatus
    EXIT_STATUS=$?
    exit $EXIT_STATUS
    ;;
  restart)
	  $SCRIPT stop
	  $SCRIPT start
    ;;
  *)
    echo $"Usage: $0 {start|stop|restart|status}"
    exit 200
esac
exit 0
