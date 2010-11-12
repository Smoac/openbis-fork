#!/bin/bash
#
# Control script for CISD Datamover on Unix / Linux systems
# -------------------------------------------------------------------------

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

#
# definitions
#

LIB_FOLDER=lib
PIDFILE=datamover.pid
CONFFILE=etc/datamover.conf
LOGFILE=log/datamover_log.txt
STARTUPLOG=log/startup_log.txt
TARGET_LOCATION_FILE=.outgoing_target_location
MARKER_SHUTDOWN_FILE=.MARKER_shutdown
SUCCESS_MSG="Self test successfully completed"
MAX_LOOPS=10

getStatus()
{
  if [ -f $PIDFILE ]; then
    PID=`cat $PIDFILE 2> /dev/null`
    isPIDRunning $PID
    if [ $? -eq 0 ]; then
      if [ -f .MARKER_shutdown ]; then
        STATUS=SHUTDOWN
        return 2
      elif [ "`ls -a1 .MARKER_*_error 2> /dev/null`" != "" ]; then
        STATUS=ERROR
        return 1
      elif [ "`ls -a1 .MARKER_*_processing 2> /dev/null`" != "" ]; then
        STATUS=PROCESSING
      else
        STATUS=IDLE
      fi 
      return 0
    else
      STATUS=STALE
      return 4
    fi
  else
    STATUS=DOWN
    return 3
  fi
}

printStatus()
{
  PID=`cat $PIDFILE 2> /dev/null`
  MSG_PREFIX="Datamover (pid $PID) is"
  case "$1" in
    ERROR)
      echo "$MSG_PREFIX running and in error state:"
      cat .MARKER_*_error 2> /dev/null
      echo
      ;;
    PROCESSING)
      echo "$MSG_PREFIX running and in processing state"
      ;;
    IDLE)
      echo "$MSG_PREFIX running and in idle state"
      ;;
    SHUTDOWN)
      echo "$MSG_PREFIX in shutdown mode"
      ;;
    DOWN)
      echo "Datamover is not running"
      ;;
    STALE)
      echo "Datamover is dead (stale pid $PID)"
      ;;
  esac
}

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
# ensure that we ignore a possible prefix "--" for any command 
command="${command#--*}"
case "$command" in
  start)
    getStatus
    EXIT_STATUS=$?
    if [ $EXIT_STATUS -lt 3 ]; then
      echo "Cannot start Datamover:"
      printStatus $STATUS
      exit 100
    fi

    echo -n "Starting Datamover "
    rotateLogFiles $LOGFILE $MAXLOGS
    shift 1
    # Build classpath from $LIB_FOLDER content.
    CP=`echo $LIB_FOLDER/*.jar | sed 's/ /:/g'`
    CMD="${JAVA_BIN} ${JAVA_OPTS} -classpath $CP ch.systemsx.cisd.datamover.Main"
    ${CMD} "$@" > $STARTUPLOG 2>&1 & echo $! > $PIDFILE
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
    echo -n "Stopping Datamover "
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
          # Shouldn't be necessary as Datamover deletes the file itself, but just to be sure
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
  shutdown)
    touch $MARKER_SHUTDOWN_FILE
  ;;
  status)
    getStatus
    EXIT_STATUS=$?
    printStatus $STATUS
    exit $EXIT_STATUS
  ;;
  mstatus)
    getStatus
    EXIT_STATUS=$?
    echo $STATUS
    exit $EXIT_STATUS
  ;;
  target)
    if [ -f $TARGET_LOCATION_FILE ]; then
      cat $TARGET_LOCATION_FILE
     else
   exit 1
    fi
  ;;
  recover)
    echo "Triggering recovery cycle"
    touch .MARKER_recovery
  ;;
  restart)
    $SCRIPT stop
    $SCRIPT start
  ;;
  help)
    ${JAVA_BIN} ${JAVA_OPTS} -jar lib/datamover.jar --help
  ;;
  version)
    ${JAVA_BIN} ${JAVA_OPTS} -jar lib/datamover.jar --version
  ;;
  test-notify)
    ${JAVA_BIN} ${JAVA_OPTS} -jar lib/datamover.jar --test-notify
  ;;
  *)
    echo $"Usage: $0 {start|stop|restart|shutdown|status|mstatus|target|recover|help|version|test-notify}"
    exit 200
esac
exit 0
