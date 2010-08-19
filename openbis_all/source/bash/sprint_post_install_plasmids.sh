#!/bin/bash
# Finishes sprint installation of plasmids server
# Warning: all changes to this file should be transfered to SVN repository

JETTY_WEBAPPS=~/sprint/openBIS-server/jetty/webapps/
CONFIG_DIR=~/config
PLASMAPPER_WEBAPP_NAME=PlasMapper

function copy_rec_or_die {
  SRC=$1
  DEST=$2
        if [ -d $SRC ]; then
                echo Copy dir $SRC to $DEST
                cp -r $SRC $DEST
        else
                echo -e "\n[ERROR]: cannot copy $SRC to $DEST, because $SRC does not exist or is not a directory"
                exit 1
        fi
}

# To make the script can be also used to update PlasMapper:
# - remove old webapp with old tmp files
# - remove old servlets from work directory
rm -fr $JETTY_WEBAPPS/$PLASMAPPER_WEBAPP_NAME
rm -fr $JETTY_WEBAPPS/../work/*$PLASMAPPER_WEBAPP_NAME*

copy_rec_or_die $CONFIG_DIR/$PLASMAPPER_WEBAPP_NAME $JETTY_WEBAPPS