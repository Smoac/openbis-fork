#! /bin/bash

if [ $UID -eq 0 ]; then
  echo "openBIS Application Server cannot run as user 'root'." > /dev/stderr
  exit 1
fi

source `dirname "$0"`/setup-env
$JVM -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     $JAVA_OPTS $JAVA_MEM_OPTS \
     -Dpython.path=$JETTY_LIB_PATH \
     -jar start.jar etc/jetty.xml lib=webapps/openbis/WEB-INF/lib >> logs/jetty.out 2>&1 &
