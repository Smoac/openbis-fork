#! /bin/sh
./shutdown.sh
java -ea -Djavax.net.ssl.trustStore=etc/openBIS.keystore -jar lib/etlserver.jar "$@" & echo $! > running.pid