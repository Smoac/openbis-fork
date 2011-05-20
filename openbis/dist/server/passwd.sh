#! /bin/bash
#
# (C) 2008, ETH Zurich, CISD
#
# Script for maintaining the password file.
# 
# Note that the password file and consequenctly this script 
# are only relevant if you set in the service.properties file
#
# authentication-service = file-authentication-service
#    
# -----------------------------------------------------------

source `dirname "$0"`/setup-env

APPLICATION_NAME=openbis

#
# change to installation directory
#
bin=$0
if [ -L $bin ]; then
  bin=`dirname $bin`/`readlink $bin`
fi
WD="`dirname $bin`"

if [ ! -x "$JVM" ]; then
  echo "No java runtime environment executable found. You need to set the environment variable JAVA_HOME appropriately."
  exit 1
fi


LIB=webapps/$APPLICATION_NAME/WEB-INF/lib
$JVM \
   -cp $LIB/cisd-args4j.jar:$LIB/commons-lang.jar:$LIB/commons-io.jar:$LIB/commons-codec.jar:$LIB/jline.jar:$LIB/log4j.jar:$LIB/$APPLICATION_NAME.jar:$LIB/screening.jar \
   ch.systemsx.cisd.authentication.file.PasswordEditorCommand "$@"
