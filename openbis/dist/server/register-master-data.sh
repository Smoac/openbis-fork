#! /bin/bash
#
# (C) 2011, ETH Zurich, CISD
#
# Executes a Jython master data registration script
# -----------------------------------------------------------

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/setup-env

APPLICATION_NAME=openbis
if [ ! -x "$JVM" ]; then
  echo "No java runtime environment executable found. You need to set the environment variable JAVA_HOME appropriately."
  exit 1
fi


LIB=$BASE/../webapps/$APPLICATION_NAME/WEB-INF/lib

$JVM \
 -cp $LIB/\* \
 ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationScriptRunnerStandalone "$@"
