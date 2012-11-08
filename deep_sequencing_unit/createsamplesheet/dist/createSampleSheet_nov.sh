#!/bin/bash
#
# Launch script for CISD openBIS Create Sample Sheet for Illumina NGS 
# Runs on Unix / Linux systems.
# -------------------------------------------------------------------------

JYTHON_FILE=createSampleSheet_nov.py

#
# change to installation directory
#
bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD=`dirname $bin`
cd $WD
SCRIPT=./`basename $0`

if [ "$JAVA_HOME" != "" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="java"
fi

ALL_JAVA_OPTS="-Djavax.net.ssl.trustStore=etc/openBIS.keystore $JAVA_OPTS"

LIB="lib"
# echo "Starting Sample Sheet Creator for openBIS Illumina NGS"
${JAVA_BIN} ${ALL_JAVA_OPTS} -cp ${LIB}/commons-codec.jar:${LIB}/commons-httpclient.jar:${LIB}/commons-logging.jar:${LIB}/dss_client.jar:${LIB}/spring.jar:${LIB}/stream-supporting-httpinvoker.jar:${LIB}/jython.jar org.python.util.jython $JYTHON_FILE "$@"