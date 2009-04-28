#! /bin/sh
./shutdown.sh
java -ea -Djavax.net.ssl.trustStore=etc/openBIS.keystore -jar lib/datastore_server.jar "$@" & echo $! > running.pid