#! /bin/bash

source jetty.properties
java -DSTOP.PORT=$JETTY_STOP_PORT -DSTOP.KEY=$JETTY_STOP_KEY -jar start.jar --stop