#! /bin/bash

source `dirname "$0"`/setup-env
$JVM -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     -Dorg.eclipse.jetty.util.log.DEBUG=true \
     $JAVA_OPTS $JAVA_MEM_OPTS \
     -jar start.jar etc/jetty.xml >> logs/jetty.out 2>&1 &
