#!/bin/bash
# saves the auth and usage logs of openBIS and DSS logs

export TOMCAT_LOGS=~openbis/sprint/openBIS-server/apache-tomcat/logs
export DSS_LOGS=~openbis/sprint/datastore_server/log
export RSYNC=/usr/bin/rsync
export DESTINATION=~openbis/logs

[ -d $DESTINATION ] || mkdir -p $DESTINATION

$RSYNC -av $TOMCAT_LOGS/*auth* $DESTINATION
$RSYNC -av $TOMCAT_LOGS/*usage* $DESTINATION
$RSYNC -av $DSS_LOGS/* $DESTINATION