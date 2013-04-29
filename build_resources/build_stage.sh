#!/bin/bash
# Builds the software components, uploads them to sprint server 
# and places a copy of them on the sprint server ready for installation. 
#
# This script assumes that you have a SSH access on 'sprint-openbis.ethz.ch'. 
# This is typically configured in the SSH config file.

if [ $# -lt 1 ]; then
    echo "Usage: $0 <year.month.x>"
    echo "Example: $0 13.04.x"
    exit 1
fi

TODAY=`date "+%Y-%m-%d"`
VER=$1
FULL_VER=$VER.stage
SPRINT_SERVER=sprint-openbis.ethz.ch

# Unset this to do a dry-run (like rsync -n) and set it to actually execute the commands
# unset EXECUTE_COMMANDS
EXECUTE_COMMANDS=1

function state_start {
	echo "----------------------------------------------------------------------"
	echo -n "| "
	echo $1
	echo ""
}

function state_end {
	echo "\ -----------------------------------"
	echo ""	
}

function setup {
	state_start Setup
  	echo "svn checkout svn+ssh://svncisd.ethz.ch/repos/cisd/build_resources/trunk build_resources"
	if [ $EXECUTE_COMMANDS ]; then
  		svn checkout svn+ssh://svncisd.ethz.ch/repos/cisd/build_resources/trunk build_resources
	fi
  
  	echo "cd build_resources"
	if [ $EXECUTE_COMMANDS ]; then
  		cd build_resources
	fi
	state_end
}

function build {
	state_start "Building openBIS..."
	
	echo "./build.sh openbis_all $FULL_VER"
	if [ $EXECUTE_COMMANDS ]; then
		./build.sh openbis_all $FULL_VER
	fi
	state_end
}

function copy_to_cisd_server {
	state_start "Copying new openBIS components to sprint-builds'..."
	
	if [ $EXECUTE_COMMANDS ]; then
	  
		OPENBIS_PATH=~openbis/fileserver/sprint_builds/openBIS
		SPRINT_DIR=$OPENBIS_PATH/$TODAY-$FULL_VER
		mkdir -p $SPRINT_DIR
		cp -p *$VER*.{zip,gz,jar} $SPRINT_DIR/
		cp -p *knime*.jar $SPRINT_DIR/
		chmod g+w -R $SPRINT_DIR
	fi
	state_end
}

if [ $EXECUTE_COMMANDS ]; then
	echo -n
else
	state_start "RUNNING DRY RUN"
fi

setup
build
copy_to_cisd_server

state_start Done!
