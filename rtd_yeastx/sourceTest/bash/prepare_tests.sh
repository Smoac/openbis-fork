#!/bin/bash
USER=`whoami`
RES=../../../datastore_server/targets

# ----- restore test database

DB=openbis_yeastx_test
FILE=yeastx-tests-openbis-database.sql

psql -U postgres -c "drop database $DB;"
psql -U postgres -c "create database $DB with owner $USER encoding = 'UNICODE';"
psql -U $USER -d $DB -f $FILE

# ----- clean target directory
 
rm -rf $RES/incoming
rm -rf $RES/store
rm -rf $RES/dropbox1
rm -rf $RES/dropbox2
 
mkdir -p $RES/incoming
mkdir -p $RES/store
mkdir -p $RES/dropbox1
mkdir -p $RES/dropbox2

# ----- compile the plugin and copy it to DSS dir
	
cd ../../build
. ./antrun.sh jar
cp ../targets/dist/datastore_server-plugins.jar ../../datastore_server/source/
