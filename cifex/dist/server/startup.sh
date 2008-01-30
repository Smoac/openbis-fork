#! /bin/bash

source jetty.properties
java -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     -Djetty.port=$JETTY_PORT \
     -Djetty.ssl.port=$JETTY_SSL_PORT \
     -Djavax.net.ssl.trustStore=etc/source-systemsx.ethz.ch.keystore \
     -jar start.jar etc/cifex-jetty.xml etc/cifex-jetty-ssl.xml >> logs/jetty.out 2>&1 &