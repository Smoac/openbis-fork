#! /bin/sh
# This is an exact copy of datamover.stop

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

if [ -f running.pid ]; then
	PID=`cat running.pid`
	isPIDRunning $PID
	if [ $? -eq 0 ]; then
		kill $PID
		echo "Previously running program with PID $PID was found and killed"
	fi
	rm running.pid
fi
