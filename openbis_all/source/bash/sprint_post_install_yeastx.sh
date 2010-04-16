#!/bin/bash
# Finishes sprint installation of yeastx software
# Warning: all changes to this file should be transfered to SVN repository

DSS=~/sprint/datastore_server
CONFIG_DIR=~/config


function copy_or_die {
  SRC=$1
  DEST=$2
	if [ -f $SRC ]; then
		echo Copy $SRC to $DEST
		cp $SRC $DEST
	else
			echo -e "\n[ERROR]: cannot copy $SRC to $DEST, because $SRC does not exist"
		exit 1
	fi
}

copy_or_die $CONFIG_DIR/eicmlDropboxProcessingPlugin.properties $DSS/etc/
copy_or_die $CONFIG_DIR/fiamlDropboxProcessingPlugin.properties $DSS/etc/

