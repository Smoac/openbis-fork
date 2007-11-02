#! /bin/sh
./shutdown.sh
java -ea -Djavax.net.ssl.trustStore=etc/lims.keystore -jar lib/etlserver.jar "$@" & echo $! > running.pid