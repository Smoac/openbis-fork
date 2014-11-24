#!/bin/sh

function replace_nonblank {
        sed  "s/enabled-modules.*=.*/enabled-modules=${string}, beewm/" $inp > changed
        mv changed $inp
}

function replace_blank {
        sed  's/enabled-modules.*=.*/enabled-modules=beewm/' $inp changed > changed
        mv changed $inp
}

function add_beewm {
	grep "beewm" $1
	if [[ $? != 0 ]] ; then
		inp=$1
		string=`grep "enabled-modules" $inp | cut -d"=" -f2`
		case $string in
			(*[^[:blank:]]*) replace_nonblank;;
			("") replace_blank;;
			(*) replace_blank
		esac
	fi
}

function fix_workflow_path {
	PLUGIN_SCRIPT=$1/core-plugins//beewm/1/dss/reporting-plugins/triggerbee/bee_client.py
	sed 's|OPENBIS_SERVERS_DIR.*=.*|OPENBIS_SERVERS_DIR = "'$1'"|' $PLUGIN_SCRIPT > changed
        mv changed $PLUGIN_SCRIPT
}

echo "Installing BeeWM"
SERVERS_DIR=$1
BEEWM_ROOT=$SERVERS_DIR/beewm
OPENBIS_DATA=$2
OPENBIS_PASS=$3
JAVA_PATH=$4

mkdir -p $BEEWM_ROOT/work/datasets
mkdir -p $BEEWM_ROOT/work/scratch

SYSTEM_CONFIG=`find $BEEWM_ROOT -name system.config`
echo "BEEWM_WORKDIR = $BEEWM_ROOT/work" >> $SYSTEM_CONFIG

OPENBIS_CONFIG=`find $BEEWM_ROOT -name openbis.config`
echo "openbis.data_dir = $OPENBIS_DATA" >> $OPENBIS_CONFIG
echo "storage.openbis.password = $OPENBIS_PASS" >> $OPENBIS_CONFIG

BEE_CONFIG=`find $BEEWM_ROOT -name bee.config`
echo "export JAVA_HOME=$JAVA_PATH" >> $BEE_CONFIG

CORETECHNOLOGIES_CONF=$SERVERS_DIR/core-plugins/core-plugins.properties 
add_beewm $CORETECHNOLOGIES_CONF

fix_workflow_path $SERVERS_DIR