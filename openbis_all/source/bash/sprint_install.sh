#!/bin/bash
# Performs the sprint components installation.
# This script assumes that you already are on the sprint server and must be run from that place
# in the home directory.
#
# If the file ~/.keystore exists it will replace openBIS.keystore of the distribution and
# the Java option -Djavax.net.ssl.trustStore=openBIS.keystore in the start up scripts will
# be removed assuming that ~/.keystore does not contains a self-signed certificate.

VER=SNAPSHOT
if [ $1 ]; then
    VER=$1
fi
SERVERS_DIR_ALIAS=sprint
SERVERS_VER=$SERVERS_DIR_ALIAS-$VER

if [ -d $SERVERS_DIR_ALIAS-* ]; then
	cd $SERVERS_DIR_ALIAS-*
	PREV_VER=${PWD#*-}
	SERVERS_PREV_VER=$SERVERS_DIR_ALIAS-$PREV_VER
	cd ..
else
	echo Warning: no previous servers instalation found.
	SERVERS_PREV_VER=unknown
fi

KEYSTORE=~/.keystore

# Unalias rm and cp commands
unalias rm
unalias cp

if [ -e $SERVERS_PREV_VER ]; then
	echo Stopping the components...
	./$SERVERS_PREV_VER/openBIS-server/apache-tomcat/bin/shutdown.sh
	./$SERVERS_PREV_VER/datastore_server/datastore_server.sh stop
fi

echo Making a database dump...
DB_NAME=openbis_productive
DB_SNAPSHOT=db_snapshots/$SERVERS_PREV_VER-$DB_NAME.sql
pg_dump -U postgres -O $DB_NAME > $DB_SNAPSHOT
tar -cf - $DB_SNAPSHOT | bzip2 > $DB_SNAPSHOT.tar.bz2
rm -f $DB_SNAPSHOT

echo Installing openBIS server...
rm -rf old/$SERVERS_PREV_VER
mv $SERVERS_PREV_VER old
rm -f $SERVERS_DIR_ALIAS
mkdir $SERVERS_VER
ln -s $SERVERS_VER $SERVERS_DIR_ALIAS
cd $SERVERS_DIR_ALIAS
unzip ../openBIS-server*$VER*
cd openBIS-server
./install.sh --nostartup $PWD ../../service.properties ../../openbis.conf
if [ -f $KEYSTORE ]; then
  cp -p $KEYSTORE apache-tomcat/openBIS.keystore
  sed 's/-Djavax.net.ssl.trustStore=openBIS.keystore //g' apache-tomcat/bin/startup.sh > new-startup.sh
  mv -f new-startup.sh apache-tomcat/bin/startup.sh
  chmod 744 apache-tomcat/bin/startup.sh
fi
apache-tomcat/bin/startup.sh

echo Installing datastore server...
cd ..
unzip ../datastore_server*$VER*
cd datastore_server
cp -p ~/datastore_server-service.properties etc/service.properties
if [ -f $KEYSTORE ]; then
  cp -p $KEYSTORE etc/openBIS.keystore
  cp -Rf ~/old/$SERVERS_PREV_VER/datastore_server/data/store/* data/store
  sed 's/-Djavax.net.ssl.trustStore=etc\/openBIS.keystore //g' datastore_server.sh > xxx
  mv -f xxx datastore_server.sh
fi
chmod 744 datastore_server.sh
export JAVA_HOME=/usr
./datastore_server.sh start

echo Doing some cleaning...
cd
mv -f *.zip tmp
rm -rf openbis

# Reset the rm command alias
alias 'rm=rm -i'
alias 'cp=cp -ipR'

echo Done!