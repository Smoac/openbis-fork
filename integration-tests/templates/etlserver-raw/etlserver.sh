#! /bin/sh
./shutdown.sh
java -Djavax.net.ssl.trustStore=etc/lims.keystore -jar lib/etlserver.jar "$@" & echo $! > running.pid