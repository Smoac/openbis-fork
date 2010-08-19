#!/bin/bash
# Finishes sprint installation of plasmids server
# Warning: all changes to this file should be transfered to SVN repository

JETTY_WEBAPPS=~/sprint/openBIS-server/jetty/webapps/
CONFIG_DIR=~/config

function copy_rec_or_die {
  SRC=$1
  DEST=$2
	if [ -f $SRC ]; then
		echo Copy $SRC to $DEST
		cp -r $SRC $DEST
	else
			echo -e "\n[ERROR]: cannot copy $SRC to $DEST, because $SRC does not exist"
		exit 1
	fi
}

copy_rec_or_die $CONFIG_DIR/PlasMapper $JETTY_WEBAPPS