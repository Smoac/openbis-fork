#! /bin/bash

# Creates a backup of openBIS AS and DSS service.properties if these files differ
# from the files in ~openbis/config
# Allows a history of service.property files
# 2010, Manuel Kohler D-BSSE

export BACKUP_DIR=~openbis/service_properties_backups
export DATE=`/bin/date +%Y-%m-%d_%H%M`
export DAYS_TO_RETAIN=100

export SP=service.properties
export DSS_SP=datastore_server-service.properties


export CONFIG_DIR=~openbis/config
export PRODUCTIVE=~openbis/sprint/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/service.properties
export DSS_PRODUCTIVE=~openbis/sprint/datastore_server/etc/service.properties

export MAIL_LIST="manuel.kohler@bsse.ethz.ch"
export BOX=`uname -n`
export PLATFORM=`uname -s`

if [ $PLATFORM = "SunOS" ];
then
  export LN=/usr/bin/ln
  export MAIL=/usr/bin/mailx
else 
  export LN=/bin/ln
  export MAIL=/bin/mailx
fi

umask 077

# openBIS AS
diff $CONFIG_DIR/$SP $PRODUCTIVE  
if [ $? -ne 0 ]; then
        echo -e "$CONFIG_DIR/$SP and $PRODUCTIVE differ!"
        echo -e "* creating a backup of $PRODUCTIVE at $DATE *"
        cp $PRODUCTIVE $BACKUP_DIR/${SP}_${BOX}_${DATE} 
        cp $PRODUCTIVE $CONFIG_DIR/$SP
        rm $BACKUP_DIR/$SP
        export LATEST_FILE=`ls -1rt $BACKUP_DIR/${SP}_* | tail -1`     
        $LN -s $LATEST_FILE $BACKUP_DIR/$SP
	$MAIL -s "$PRODUCTIVE changed!" manuel.kohler@bsse.ethz.ch < $LATEST_FILE 
fi

# openBIS DSS
diff $CONFIG_DIR/$DSS_SP $DSS_PRODUCTIVE
if [ $? -ne 0 ]; then
        echo -e "$CONFIG_DIR/service.properties $DSS_PRODUCTIVE"
        echo -e "* creating a backup of $DSS_PRODUCTIVE at $DATE "
        cp $DSS_PRODUCTIVE $BACKUP_DIR/${DSS_SP}_${BOX}_${DATE}
        cp $DSS_PRODUCTIVE $CONFIG_DIR/$DSS_SP
        rm $BACKUP_DIR/$DSS_SP
        export LATEST_FILE=`ls -1rt  $BACKUP_DIR/${DSS_SP}_* | tail -1`     
        $LN -s $LATEST_FILE $BACKUP_DIR/$DSS_SP
	$MAIL -s "$DSS_PRODUCTIVE changed!" manuel.kohler@bsse.ethz.ch < $LATEST_FILE 
fi

#/usr/bin/find $BACKUP_DIR -type f -mtime +$DAYS_TO_RETAIN -exec rm {} \;
echo " ** Finished ** "
