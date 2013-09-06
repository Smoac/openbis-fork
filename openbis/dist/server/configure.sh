#!/bin/bash

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

command=$1

case "$command" in
	log-service-calls)
 	mkdir -p ../.control
	if [ "$2" == "off" ]; then
		touch ../.control/log-service-call-start-off
		echo "Switched off logging of service calls."
	else
		touch ../.control/log-service-call-start-on
		echo "Switched on logging of service calls."
	fi
	;;    
	*)
	echo "Usage: $0 {log-service-calls}"
	exit 200
	;;
esac
exit 0
