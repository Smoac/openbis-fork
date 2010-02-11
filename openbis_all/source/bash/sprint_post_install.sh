#!/bin/sh
# Central post install script for all openBIS servers

export BIN=~openbis/bin
export SERVER=`uname -n`

export SPRINT=cisd-bamus
export DEMO=cisd-tongariro
export YEASTX=imsb-us-openbis.ethz.ch
export PHOSPHONETX=openbis-phosphonetx.ethz.ch
export LIVERX=openbis-liverx.ethz.ch
export AGRONOMICS=bs-dsvr11.ethz.ch
export DSU=openbis-dsu
export SCU=openbis-scu
export BASYSBIO=bs-dsvr10.ethz.ch

function create_individual_greeting_message {
# Creates an individual greeting message
	if [ -f ~openbis/config/openbis_instance.txt ]; then
	   export OPENBIS_DICT=~openbis/sprint/openBIS-server/apache-tomcat/webapps/openbis/common-dictionary.js
	   export SERVER_INSTANCE_NAME=`cat ~openbis/config/openbis_instance.txt`
	   perl -pe 's/openbis_instance: "",/openbis_instance: "$ENV{SERVER_INSTANCE_NAME}",/' -i $OPENBIS_DICT
	fi
}

function restore_loginHeader {
  if [ -f ~openbis/config/loginHeader.html ]; then
    echo restoring loginHeader.html
    cp -r ~openbis/config/images ~openbis/sprint/openBIS-server/apache-tomcat/webapps/openbis/
    cp ~openbis/config/loginHeader.html ~openbis/sprint/openBIS-server/apache-tomcat/webapps/openbis/
    cp ~openbis/config/help.html ~openbis/sprint/openBIS-server/apache-tomcat/webapps/openbis/
  fi
}

case "$SERVER" in

	$SPRINT)
	echo SPRINT:$SPRINT;
	restore_loginHeader
	create_individual_greeting_message
	
	$BIN/sprint_post_install_sprint.sh
	;;
	$DEMO)
	restore_loginHeader
	create_individual_greeting_message
	echo DEMO:$DEMO;
	$BIN/sprint_post_install_demo.sh
	;;
	$YEASTX)
	restore_loginHeader
	create_individual_greeting_message
	echo YEASTX:$YEASTX;
	$BIN/sprint_post_install_yeastx.sh
	;;
	$PHOSPHONETX)
	restore_loginHeader
	create_individual_greeting_message
	echo PHOSPHONETX:$PHOSPHONETX;
	;;
	$LIVERX)
	restore_loginHeader
	create_individual_greeting_message
	echo LIVERX:$LIVERX;
	;;
	$AGRONOMICS)
	echo AGRONOMICS:$AGRONOMICS;
	restore_loginHeader
	create_individual_greeting_message
	unzip -q ~openbis/config/
	mv ~openbis/datastore_server-plugins.jar ~openbis/sprint/datastore_server/lib
	$BIN/sprint_post_install_agronomics.sh
	;;
	$DSU)
	echo DSU:$DSU;
	restore_loginHeader
	create_individual_greeting_message
	;;
	$SCU)
	echo SCU:$SCU;
	restore_loginHeader
	create_individual_greeting_message
	;;
	$BASYSBIO)
	echo BASYSBIO:$BASYSBIO;
	restore_loginHeader
	create_individual_greeting_message
	mv ~openbis/datastore_server-plugins.jar ~openbis/sprint/datastore_server/lib
	$BIN/sprint_post_install_basysbio.sh
	;;
	*)
	echo Wrong Server! $SERVER is not in the list of openBIS Servers.;
	exit 1;
	;;
esac
