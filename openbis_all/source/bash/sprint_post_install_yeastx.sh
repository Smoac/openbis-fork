#!/bin/bash
# Finishes sprint installation of yeastx software
# Warning: all changes to this file should be transfered to SVN repository

function copy_or_die {
  SRC=$1
  DEST=$2
	if [ -f $SRC ]; then
		echo Copy $SRC to $DEST
		cp $SRC $DEST
	else
		echo Error: cannot copy $SRC to $DEST, because $SRC does not exist
		exit 1
	fi
}

DSS=sprint/datastore_server

copy_or_die ~/eicmlDropboxProcessingPlugin.properties $DSS/etc/
copy_or_die ~/fiamlDropboxProcessingPlugin.properties $DSS/etc/
copy_or_die ~/datastore_server-plugins.jar $DSS/lib/

