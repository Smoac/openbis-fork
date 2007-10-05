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
STARTUPLOG=log/startup_log.txt

#
# change to installation directory
#
bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD=`dirname $bin`
cd $WD

#
# source configuration script, if any
#
test -f $CONFFILE && source $CONFFILE
if [ "$JAVA_HOME" != "" ]; then
	JAVA_HOME="$JAVA_HOME/bin/"
fi

case "$1" in
        start)
	        echo -n "Starting Datamover "

		shift 1		
		${JAVA_HOME}java -jar lib/datamover.jar "$@" > $STARTUPLOG 2>&1 & echo $! > $PIDFILE
		if [ $? -eq 0 ]; then
			# wait for initial self-test to finish"
			sleep 1
			PID=`cat $PIDFILE`
			isPIDRunning $PID
			if [ $? -eq 0 ]; then
				echo "(pid $PID)"
			else
				rm $PIDFILE
				echo "FAILED - see $WD/$STARTUPLOG for details"
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
	        $0 stop
	        $0 start
        ;;
	version)
                ${JAVA_HOME}java -jar lib/datamover.jar --version
	;;
        *)
        echo $"Usage: $0 {start|stop|restart|status|recover|version}"
        exit 1
esac
exit 0
