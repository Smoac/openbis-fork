#!/bin/bash
# Performs the sprint components installation.
# This script assumes that you already are on the sprint server and must be run from that place
# in the home directory.

VER=SNAPSHOT
if [ $1 ]; then
    VER=$1
fi
cd sprint-*
PREV_VER=${PWD#*-}
cd -

DB_SNAPSHOT=db_snapshots/sprint$PREV_VER-lims_productive.sql
KEYSTORE=~/.keystore

# Unalias rm and cp commands
unalias rm
unalias cp

echo Stopping the components...
./sprint/openBIS-server/apache-tomcat/bin/shutdown.sh
./sprint/datastore_server/datastore_server.sh stop

echo Making a database dump...
pg_dump -U postgres -O lims_productive > $DB_SNAPSHOT
tar -cf - $DB_SNAPSHOT | bzip2 >db_snapshots/sprint$PREV_VER-lims_productive.tar.bz2
rm -f $DB_SNAPSHOT

echo Installing openBIS server...
rm -rf old/sprint-$PREV_VER
mv sprint-$PREV_VER old
rm -f sprint
mkdir sprint-$VER
ln -s sprint-$VER sprint
cd sprint
unzip ../openBIS-server*$VER*
cd openBIS-server
./install.sh --nostartup $PWD ../../service.properties
cp -p $KEYSTORE apache-tomcat/openBIS.keystore
sed 's/-Djavax.net.ssl.trustStore=openBIS.keystore //g' apache-tomcat/bin/startup.sh > new-startup.sh
mv -f new-startup.sh apache-tomcat/bin/startup.sh
chmod 744 apache-tomcat/bin/startup.sh
apache-tomcat/bin/startup.sh

echo Installing datastore server...
cd ..
unzip ../datastore_server*$VER*
cd datastore_server
cp -p ~/datastore_server-service.properties etc/service.properties
cp -p $KEYSTORE etc/openBIS.keystore
mv -f ~/old/sprint-$PREV_VER/datastore_server/data/store/* data/store
sed 's/-Djavax.net.ssl.trustStore=etc\/openBIS.keystore //g' datastore_server.sh > xxx
mv -f xxx datastore_server.sh
chmod 744 datastore_server.sh
export JAVA_HOME=/usr
./datastore_server.sh start

echo Doing some cleaning...
cd
mv -f *.zip tmp
rm -rf openbis

# Reset the rm command alias
alias 'rm=rm -i'
alias 'cp=cp -i'

echo Done!