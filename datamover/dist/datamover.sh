#!/bin/bash
#
# Control script for CISD Datamover on UNIX / Linux systems
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

#
# definitions
#

PIDFILE=datamover.pid
CONFFILE=etc/datamover.conf
LOGFILE=log/datamover_log.txt
STARTUPLOG=log/startup_log.txt
SUCCESS_MSG="Self test successfully completed"
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
# ensure that we ignore a possible prefix "--" for any command 
command="${command#--*}"
case "$command" in
        start)
	        echo -n "Starting Datamover "

		shift 1
		${JAVA_BIN} ${JAVA_OPTS} -jar lib/datamover.jar "$@" > $STARTUPLOG 2>&1 & echo $! > $PIDFILE
		if [ $? -eq 0 ]; then
			# wait for initial self-test to finish
			n=0
			while [ $n -lt $MAX_LOOPS ]; do
				sleep 1
				grep "$SUCCESS_MSG" $LOGFILE > /dev/null 2>&1
				if [ $? -eq 0 ]; then
					break
				fi
				n=$(($n+1))
			done 
			PID=`cat $PIDFILE`
			isPIDRunning $PID
			if [ $? -eq 0 ]; then
				grep "$SUCCESS_MSG" $LOGFILE > /dev/null 2>&1
				if [ $? -ne 0 ]; then
					echo "(pid $PID - WARNING: SelfTest not yet finished)"
				else
					echo "(pid $PID)"
				fi
			else
				rm $PIDFILE
				echo "FAILED"
				echo "startup log says:"
				cat $WD/$STARTUPLOG
			fi
		else
			echo "FAILED"
		fi
		;;
        stop)
        	echo -n "Stopping Datamover "
		if [ -f $PIDFILE ]; then
			PID=`cat $PIDFILE`
			isPIDRunning $PID
			if [ $? -eq 0 ]; then
				kill $PID
				if [ $? -eq 0 ]; then
					echo "(pid $PID)"
					rm $PIDFILE
				else
					echo "FAILED"
				fi
			else
				rm $PIDFILE
				echo "(was dead - cleaned up pid file)"
			fi
		else
			echo "(not running - nothing to do)"
		fi
        ;;
        status)
		if [ -f $PIDFILE ]; then
			PID=`cat $PIDFILE`
			isPIDRunning $PID
			if [ $? -eq 0 ]; then
				echo "Datamover is running (pid $PID)"
			else
				echo "Datamover is dead (stale pid $PID)"
			fi
		else
			echo "Datamover is not running"
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
        echo $"Usage: $0 {start|stop|restart|status|recover|help|version|test-notify}"
        exit 1
esac
exit 0
