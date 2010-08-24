#!/bin/bash
# makes a backup of all configuration files to the specified directory

CONF=$1
mkdir -p $CONF

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ROOT=$BASE/../servers

cp $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties $CONF/service.properties
cp $ROOT/datastore_server/etc/service.properties $CONF/datastore_servere-service.properties
cp $ROOT/datastore_server/etc/tabular-data-graph.properties $CONF/tabular-data-graph.properties
#cp $ROOT/datastore_server/etc/log.xml $CONF/log.xml
cp $ROOT/openBIS-server/jetty/bin/passwd.sh $CONF/passwd.sh
cp $ROOT/datastore_server/etc/datastore_server.conf $CONF/datastore_server.conf
cp $ROOT/openBIS-server/jetty/bin/openbis.conf $CONF/openbis.conf
cp $ROOT/openBIS-server/jetty/etc/passwd $CONF/passwd
cp $ROOT/openBIS-server/jetty/etc/jetty.xml $CONF/jetty.xml
cp $ROOT/openBIS-server/jetty/etc/openBIS.keystore $CONF/.keystore 
cp $ROOT/openBIS-server/jetty/bin/jetty.properties $CONF/jetty.properties