#!/bin/bash
# Fetches "openBIS for HCS" administration scripts from source repository to the bin folder in current directory.

# setup scripts for the generic openBIS version 
SVN_DIR_GENERIC=svncisd.ethz.ch/repos/cisd/openbis/trunk/dist/admin

#setup scripts for screening version
SVN_DIR_SCREENING=svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin


BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

SERVERS_DIR=$BASE/servers

if [ ! -d "$SERVERS_DIR" ]; then
    echo "$SERVERS_DIR does not exist. Aborting ..."
    exit 1
fi

if [ $(find "$SERVERS_DIR" -name "*server-screening*.zip")  ]; then
    
    echo "Downloading admin scripts for openBIS screening..." 
    SVN_DIR=$SVN_DIR_SCREENING
    
elif [ $(find "$SERVERS_DIR" -name "*server-*.zip")  ]; then 
    
    echo "Downloading admin scripts for generic openBIS installation..." 
    SVN_DIR=$SVN_DIR_GENERIC
     
else
    echo "Could not detect openBIS archives in $SERVERS_DIR. Please, download openBIS and datastore server archives and try again..."
    exit 2 
fi


mkdir bin
cd bin

wget $SVN_DIR/svn-update.sh
wget $SVN_DIR/env

# fetch screening specific file
if [ "$SVN_DIR" == "$SVN_DIR_SCREENING" ]; then
    wget $SVN_DIR/empty-screening-database.sql
fi


source ./svn-update.sh

cd ..