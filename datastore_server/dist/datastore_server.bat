@echo off

java -Djavax.net.ssl.trustStore=etc\openBIS.keystore -jar lib\datastore_server.jar %1 %2 %3 %4 %5 %6 %7
