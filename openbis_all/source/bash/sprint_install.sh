#!/bin/bash
# Performs the sprint components installation.
# This script assumes that you already are on the sprint server and must be run from that place
# in the home directory.
#
# If the file ~/.keystore exists it will replace openBIS.keystore of the distribution and
# the Java option -Djavax.net.ssl.trustStore=openBIS.keystore in the start up scripts will
# be removed assuming that ~/.keystore does not contains a self-signed certificate.

CONFIG_DIR=~openbis/config
KEYSTORE=~openbis/.keystore
SERVERS_DIR_ALIAS=sprint
VER=SNAPSHOT
DATE=`/bin/date +%Y-%m-%d_%H%M`
DB_NAME=openbis_productive
DB_SNAPSHOT=~openbis/db_backups
SERVER_DIR=~openbis/sprint/openBIS-server/jetty
DAYS_TO_RETAIN=35

if [ $1 ]; then
    VER=$1
fi
SERVERS_VER=$SERVERS_DIR_ALIAS-$VER

if [ -d $SERVERS_DIR_ALIAS-*/ ]; then
        cd $SERVERS_DIR_ALIAS-*/
        PREV_VER=${PWD#*-}
        SERVERS_PREV_VER=$SERVERS_DIR_ALIAS-$PREV_VER
        cd ..
else
        echo Warning: no previous servers installation found. Initial installation?
        SERVERS_PREV_VER=unknown
fi

# Unalias rm and cp commands
alias cp='cp'
alias rm='rm'

if [ -e $SERVERS_PREV_VER ]; then
        echo Stopping the components...
        ./$SERVERS_PREV_VER/openBIS-server/jetty/bin/shutdown.sh
        ./$SERVERS_PREV_VER/datastore_server/datastore_server.sh stop
fi

echo Making a database dump...
# A custom-format dump (-Fc flag) is not a script for psql, but instead must be
# restored with pg_restore, for example:
# pg_restore -d dbname filename
pg_dump -Uopenbis -Fc $DB_NAME > $DB_SNAPSHOT/$SERVERS_PREV_VER-$DB_NAMEi_${DATE}.dmp

# we actually need to clean that up from time to time
/usr/bin/find $DB_SNAPSHOT -type f -mtime +$DAYS_TO_RETAIN -exec rm {} \;

echo Installing openBIS server...
rm -rf old/$SERVERS_PREV_VER
mv $SERVERS_PREV_VER old
rm -f $SERVERS_DIR_ALIAS
mkdir $SERVERS_VER
ln -s $SERVERS_VER $SERVERS_DIR_ALIAS
cd $SERVERS_DIR_ALIAS
unzip -q ../openBIS-server*$VER*
cd openBIS-server
./install.sh $PWD $CONFIG_DIR/service.properties $CONFIG_DIR/openbis.conf
if [ -f $KEYSTORE ]; then
  cp -p $KEYSTORE jetty/etc/openBIS.keystore
fi
if [ -f $CONFIG_DIR/capabilities ]; then
  cp -p $CONFIG_DIR/capabilities jetty/etc
fi


echo Installing datastore server...
cd ..
unzip -q ../datastore_server-*$VER*
for file in ../datastore_server_plugin-*$VER*; do 
	if [ -f $file ]; then 
		unzip -q $file;
	fi
done
cd datastore_server
cp -p $CONFIG_DIR/datastore_server-service.properties etc/service.properties
if [ -f $KEYSTORE ]; then
  cp -p $KEYSTORE etc/openBIS.keystore
fi
chmod 744 datastore_server.sh
export JAVA_HOME=/usr

cd ~openbis
mv -f *.zip old/
rm -rf openbis
cd ~openbis/sprint/openBIS-server
rm jetty.zip install.sh openbis.conf openBIS.keystore openBIS.war passwd.sh jetty.xml service.properties jetty-version.txt setup-env web-client.properties shutdown.sh startup.sh

# Reset the rm command alias
alias 'rm=rm -i'
alias 'cp=cp -ipR'

echo Running ~openbis/bin/sprint_post_install.sh
~openbis/bin/sprint_post_install.sh

echo "Done, now start the servers using all-up.sh"
