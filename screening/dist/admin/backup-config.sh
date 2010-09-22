#!/bin/bash
# makes a backup of all configuration files to the specified directory

CONF=$1
if [ "$CONF" == "" ]; then
	echo Error: directory where configuration should be backed up has not been specified! 
	exit 1
fi
mkdir -p $CONF

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ROOT=$BASE/../servers

# -- AS
cp $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties $CONF/
cp $ROOT/openBIS-server/jetty/etc/log.xml $CONF/
cp $ROOT/openBIS-server/jetty/bin/openbis.conf $CONF/
cp $ROOT/openBIS-server/jetty/etc/jetty.xml $CONF/
cp $ROOT/openBIS-server/jetty/bin/jetty.properties $CONF/
# not always present
cp $ROOT/openBIS-server/jetty/etc/openBIS.keystore $CONF/.keystore 
cp $ROOT/openBIS-server/jetty/etc/passwd $CONF/
cp $ROOT/openBIS-server/jetty/etc/web-client.properties $CONF/

# -- DSS
cp $ROOT/datastore_server/etc/service.properties $CONF/dss-service.properties
cp $ROOT/datastore_server/etc/log.xml $CONF/dss-log.xml
cp $ROOT/datastore_server/etc/datastore_server.conf $CONF/datastore_server.conf
# not always present
cp $ROOT/datastore_server/etc/openBIS.keystore $CONF/.keystore 
# screening-specific
cp $ROOT/datastore_server/etc/tabular-data-graph.properties $CONF/tabular-data-graph.properties
