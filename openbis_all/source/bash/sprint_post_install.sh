#!/bin/sh
# Central post install script for all openBIS servers


export SERVER=`uname -n`

export SPRINT=cisd-bamus
export DEMO=cisd-tongariro
export YEASTX=imsb-us-openbis.ethz.ch
export PHOSPHONETX=cisd-openbis-phosphonetx.ethz.ch
export LIVERX=cisd-openbis-liverx.ethz.ch
export AGRONOMICS=bs-dsvr11-s.ethz.ch
export BSSE=openbis-dsu
export BASYSBIO=bs-dsvr10.ethz.ch

function create_individual_greeting_message {
# Creates an individual greeting message
	if [ -f ~/config/openbis_instance.txt ]; then
	   export OPENBIS_DICT=~/sprint/openBIS-server/apache-tomcat/webapps/openbis/common-dictionary.js
	   export SERVER_INSTANCE_NAME=`cat ~/config/openbis_instance.txt`
	   perl -pe 's/Welcome to openBIS/Welcome to $ENV{SERVER_INSTANCE_NAME} openBIS/' -i $OPENBIS_DICT
	fi
}

case "$SERVER" in

	$SPRINT)
	echo SPRINT:$SPRINT;
	./sprint_post_install_sprint.sh
	;;
	$DEMO)
	echo DEMO:$DEMO;
	./sprint_post_install_demo.sh
	;;
	$YEASTX)
	echo YEASTX:$YEASTX;
	./sprint_post_install_yeastx.sh
	;;
	$PHOSPHONETX)
	echo PHOSPHONETX:$PHOSPHONETX;
	;;
	$LIVERX)
	echo LIVERX:$LIVERX;
	;;
	$AGRONOMICS)
	echo AGRONOMICS:$AGRONOMICS;
	./sprint_post_install_yeastx.sh	
	;;
	$BSSE)
	echo BSSE:$BSSE;
	;;
	$BASYSBIO)
	echo BASYSBIO:$BASYSBIO;
	create_individual_greeting_message
	./sprint_post_install_basysbio.sh
	;;
	*)
	echo Wrong Server! $SERVER is not in the list of openBIS Servers.;
	exit 1;
	;;
esac
echo DONE
