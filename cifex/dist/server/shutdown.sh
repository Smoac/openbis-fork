#! /bin/bash

# Load properties that become environment variables
# NOTE: it would be possible to specify a normal java properties file after the 'start.jar'

JETTY_BIN_DIR=`dirname "$0"`
if [ ${JETTY_BIN_DIR#/} == ${JETTY_BIN_DIR} ]; then
    JETTY_BIN_DIR="`pwd`/${JETTY_BIN_DIR}"
fi

source "$JETTY_BIN_DIR"/jetty.properties
cd "$JETTY_BIN_DIR"/..

java -DSTOP.PORT=$JETTY_STOP_PORT -DSTOP.KEY=$JETTY_STOP_KEY -jar start.jar --stop