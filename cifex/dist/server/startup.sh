#! /bin/bash

# Load properties that become environment variables
# NOTE: it would be possible to specify a normal java properties file after the 'start.jar'

JETTY_BIN_DIR=`dirname "$0"`
if [ ${JETTY_BIN_DIR#/} == ${JETTY_BIN_DIR} ]; then
    JETTY_BIN_DIR="`pwd`/${JETTY_BIN_DIR}"
fi

source "$JETTY_BIN_DIR"/jetty.properties
cd "$JETTY_BIN_DIR"/..

java -DSTOP.PORT=$JETTY_STOP_PORT \
     -DSTOP.KEY=$JETTY_STOP_KEY \
     -Djetty.port=$JETTY_PORT \
     -Djetty.ssl.port=$JETTY_SSL_PORT \
     -Djavax.net.ssl.trustStore=etc/source-systemsx.ethz.ch.keystore \
     -jar start.jar etc/cifex-jetty.xml etc/cifex-jetty-ssl.xml >> logs/jetty.out 2>&1 &