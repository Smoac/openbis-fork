#!/bin/bash
# saves the auth and usage logs of openBIS and DSS logs

export AS_LOGS=~openbis/sprint/openBIS-server/jetty/logs
export DSS_LOGS=~openbis/sprint/datastore_server/log
export RSYNC=/usr/bin/rsync
export DESTINATION=~openbis/logs
export DAYS_TO_RETAIN=100

[ -d $DESTINATION ] || mkdir -p $DESTINATION

$RSYNC -av $AS_LOGS/*auth* $DESTINATION
$RSYNC -av $AS_LOGS/*usage* $DESTINATION
$RSYNC -av $DSS_LOGS/* $DESTINATION

if [ -f $DESTINATION/check_logins.sh ]; then
	$DESTINATION/check_logins.sh
fi

/usr/bin/find $DESTINATION/*txt* -type f -mtime +$DAYS_TO_RETAIN -exec rm {} \;
