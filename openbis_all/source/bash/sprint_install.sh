#!/bin/bash
# Performs the sprint components installation.
# This script assumes that you already are on the sprint server and must be run from that place
# in the home directory.
#
# If the file ~/.keystore exists it will replace openBIS.keystore of the distribution and
# the Java option -Djavax.net.ssl.trustStore=openBIS.keystore in the start up scripts will
# be removed assuming that ~/.keystore does not contains a self-signed certificate.

BASE=/home/openbis
CONFIG_DIR=$BASE/config
KEYSTORE=$BASE/.keystore
SERVERS_DIR_ALIAS=sprint
VER=SNAPSHOT
DATE=`/bin/date +%Y-%m-%d_%H%M`
DB_NAME=openbis_productive
DB_SNAPSHOT=$BASE/db_backups
TOMCAT_DIR=$BASE/sprint/openBIS-server/apache-tomcat
DAYS_TO_RETAIN=35

cd $BASE

if [ $1 ]; then
    VER=$1
fi
SERVERS_VER=$SERVERS_DIR_ALIAS-$VER

if [ -d $SERVERS_DIR_ALIAS-* ]; then
	cd $SERVERS_DIR_ALIAS-*
	PREV_VER=${PWD#*-}
	SERVERS_PREV_VER=$SERVERS_DIR_ALIAS-$PREV_VER
	cd ..
else
	echo Warning: no previous servers installation found. Initial installation?
	SERVERS_PREV_VER=unknown
fi

# Unalias rm and cp commands
unalias rm
unalias cp

if [ -e $SERVERS_PREV_VER ]; then
	echo Stopping the components...
	./$SERVERS_PREV_VER/openBIS-server/apache-tomcat/bin/shutdown.sh
	./$SERVERS_PREV_VER/datastore_server/datastore_server.sh stop
fi

echo Making a database dump...
# A custom-format dump (-Fc flag) is not a script for psql, but instead must be
# restored with pg_restore, for example:
# pg_restore -d dbname filename
pg_dump -Uopenbis -Fc $DB_NAME > $DB_SNAPSHOT/$SERVERS_PREV_VER-$DB_NAMEi_${DATE}.dmp
# we actually need to clean that up from time to time
# this is cleaned by the nightly backup script
#/usr/bin/find $DB_SNAPSHOT -type f -mtime +$DAYS_TO_RETAIN -exec rm {} \;

echo Installing openBIS server...
rm -rf old/$SERVERS_PREV_VER
mv $SERVERS_PREV_VER old
rm -f $SERVERS_DIR_ALIAS
mkdir $SERVERS_VER
ln -s $SERVERS_VER $SERVERS_DIR_ALIAS
cd $SERVERS_DIR_ALIAS
unzip ../openBIS-server*$VER*
cd openBIS-server
./install.sh --nostartup $PWD $CONFIG_DIR/service.properties $CONFIG_DIR/openbis.conf
if [ -f $KEYSTORE ]; then
  cp -p $KEYSTORE apache-tomcat/openBIS.keystore
  sed 's/-Djavax.net.ssl.trustStore=openBIS.keystore //g' apache-tomcat/bin/startup.sh > new-startup.sh
  mv -f new-startup.sh apache-tomcat/bin/startup.sh
  chmod 744 apache-tomcat/bin/startup.sh
fi
#apache-tomcat/bin/startup.sh

echo Installing datastore server...
cd ..
unzip ../datastore_server*$VER*
cd datastore_server
cp -p $CONFIG_DIR/datastore_server-service.properties etc/service.properties
if [ -f $KEYSTORE ]; then
  cp -p $KEYSTORE etc/openBIS.keystore
  cp -Rf $BASE/old/$SERVERS_PREV_VER/datastore_server/data/store/* data/store
  sed 's/-Djavax.net.ssl.trustStore=etc\/openBIS.keystore //g' datastore_server.sh > xxx
  mv -f xxx datastore_server.sh
fi
chmod 744 datastore_server.sh
export JAVA_HOME=/usr
#./datastore_server.sh start

#echo Doing some cleaning...
cd
mv -f *.zip old
rm -rf openbis
cd $BASE/sprint/openBIS-server
rm apache-tomcat.zip install.sh openbis.conf openBIS.keystore openBIS.war passwd.sh server.xml service.properties tomcat-version.txt
/usr/bin/find $BASE/old -mtime +$DAYS_TO_RETAIN -exec rm -rf {} \;


# Reset the rm command alias
alias 'rm=rm -i'
alias 'cp=cp -ipR'

echo Running $BASE/bin/sprint_post_install.sh
$BASE/bin/sprint_post_install.sh

echo Done, run 'has-config-changed.sh' and start the servers!
