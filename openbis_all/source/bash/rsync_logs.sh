#!/bin/bash
# saves the auth and usage logs of openBIS

export TOMCAT_LOGS=~/sprint/openBIS-server/apache-tomcat/logs
export RSYNC=/usr/bin/rsync
export DESTINATION=/local0/home/openbis/tomcat_logs

[ -d $DESTINATION ] || mkdir -p $DESTINATION

$RSYNC -av $TOMCAT_LOGS/*auth* $DESTINATION
$RSYNC -av $TOMCAT_LOGS/*usage* $DESTINATION
