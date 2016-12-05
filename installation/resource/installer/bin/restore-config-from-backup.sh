#!/bin/bash
# Overrides all configuration files with those which can be found in the specified config backup directory.
# Assumes that openbis is installed in the parent directory of the directory where this script is located. 

CONF=$1
if [ "$CONF" == "" ]; then
  echo Error: directory from which configuration should be restored has not been specified! 
  exit 1
fi

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/common-functions.sh
ROOT=$BASE/../servers

echo "Restoring configuration backup from $CONF to $ROOT ..."

# -- AS
if [ -d $ROOT/openBIS-server ]; then
    cp $CONF/service.properties $ROOT/openBIS-server/jetty/etc/
    cp $CONF/log.xml $ROOT/openBIS-server/jetty/etc/ 
    cp $CONF/openbis.conf $ROOT/openBIS-server/jetty/etc/
    cp $CONF/jetty.properties $ROOT/openBIS-server/jetty/etc/

    # for 13.04.10 and older
    copyIfExists $CONF/../openBIS-server/jetty/webapps/openbis/welcomePageSimple.html $ROOT/openBIS-server/jetty/webapps/openbis/custom/welcomePageSimpleGeneric.html
    
    # for 13.04.11 and later
    copyIfExists $CONF/../openBIS-server/jetty/webapps/openbis/custom/welcomePageSimpleGeneric.html $ROOT/openBIS-server/jetty/webapps/openbis/custom

    # not always present
    copyIfExists $CONF/.keystore $ROOT/openBIS-server/jetty/etc/openBIS.keystore
    copyIfExists $CONF/passwd $ROOT/openBIS-server/jetty/etc/
    copyIfExists $CONF/web-client.properties $ROOT/openBIS-server/jetty/etc/
    copyIfExists $CONF/capabilities $ROOT/openBIS-server/jetty/etc/
    copyIfExists $CONF/dss-datasource-mapping $ROOT/openBIS-server/jetty/etc/
    copyConfig $CONF/core-plugins "html/etc$" $ROOT/core-plugins
    copyFolderIfExists $CONF/start.d $ROOT/openBIS-server/jetty/start.d
fi

# -- DSS
cp $CONF/dss-service.properties $ROOT/datastore_server/etc/service.properties
cp $CONF/dss-log.xml $ROOT/datastore_server/etc/log.xml
cp $CONF/datastore_server.conf $ROOT/datastore_server/etc/
# not always present
copyIfExists $CONF/.keystore $ROOT/datastore_server/etc/openBIS.keystore
copyIfExists $CONF/ext-lib $ROOT/datastore_server 

# -- ELN-LIMS
if [ -d $ROOT/core-plugins/eln-lims/1/as/webapps/eln-lims/html/etc ]; then
  if [ -d $CONF/../eln-lims/1/as/webapps/eln-lims/html/etc ]; then
    cp $CONF/../eln-lims/1/as/webapps/eln-lims/html/etc/* $ROOT/core-plugins/eln-lims/1/as/webapps/eln-lims/html/etc/
  fi
fi