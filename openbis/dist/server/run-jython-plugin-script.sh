#! /bin/bash
#
# (C) 2011, ETH Zurich, CISD
#
# Executes a Jython plugin script
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
WEBINF_CLASSES=$BASE/../webapps/$APPLICATION_NAME/WEB-INF/classes

$JVM \
 -cp $LIB/*.jar:$WEBINF_CLASSES \
 ch.systemsx.cisd.openbis.plugin.jython.api.v1.impl.JythonPluginScriptStandalone "$@"
