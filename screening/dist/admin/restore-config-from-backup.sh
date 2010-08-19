#!/bin/bash
# Overrides all configuration files with those which can be found in the specified config backup directory.
# Assumes that openbis is installed in the parent directory of the directory where this script is located. 

CONF=$1

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ROOT=$BASE/..

cp $CONF/service.properties $ROOT/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties
cp $CONF/datastore_servere-service.properties $ROOT/datastore_server/etc/service.properties
cp $CONF/tabular-data-graph.properties $ROOT/datastore_server/etc/tabular-data-graph.properties
#cp $CONF/log.xml $ROOT/datastore_server/etc/log.xml
cp $CONF/passwd.sh $ROOT/openBIS-server/jetty/bin/passwd.sh
cp $CONF/datastore_server.conf $ROOT/datastore_server/etc/datastore_server.conf
cp $CONF/openbis.conf $ROOT/openBIS-server/jetty/bin/openbis.conf
cp $CONF/passwd $ROOT/openBIS-server/jetty/etc/passwd
cp $CONF/jetty.xml $ROOT/openBIS-server/jetty/etc/jetty.xml
cp $CONF/.keystore $ROOT/datastore_server/etc/openBIS.keystore
cp $CONF/.keystore $ROOT/openBIS-server/jetty/etc/openBIS.keystore
