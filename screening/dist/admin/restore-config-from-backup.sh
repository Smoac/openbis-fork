#!/bin/bash
# Overrides all configuration files with those which can be found in the specified config backup directory.
# Assumes that openbis is installed in the parent directory of the directory where this script is located. 

CONF=$1
if [ "$CONF" == "" ]; then
	echo Error: directory from which configuration should be restored has not been specified! 
	exit 1
fi

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ROOT=$BASE/../servers

# -- AS
cp $CONF/service.properties $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties
cp $CONF/log.xml $ROOT/openBIS-server/jetty/etc/log.xml 
cp $CONF/openbis.conf $ROOT/openBIS-server/jetty/bin/openbis.conf
cp $CONF/jetty.xml $ROOT/openBIS-server/jetty/etc/jetty.xml
cp $CONF/jetty.properties $ROOT/openBIS-server/jetty/bin/jetty.properties
# not always present
cp $CONF/.keystore $ROOT/openBIS-server/jetty/etc/openBIS.keystore
cp $CONF/passwd $ROOT/openBIS-server/jetty/etc/passwd

# -- DSS
cp $CONF/dss-service.properties $ROOT/datastore_server/etc/service.properties
cp $CONF/dss-log.xml $ROOT/datastore_server/etc/log.xml
cp $CONF/datastore_server.conf $ROOT/datastore_server/etc/datastore_server.conf
# not always present
cp $CONF/.keystore $ROOT/datastore_server/etc/openBIS.keystore
# screening-specific
cp $CONF/tabular-data-graph.properties $ROOT/datastore_server/etc/tabular-data-graph.properties
