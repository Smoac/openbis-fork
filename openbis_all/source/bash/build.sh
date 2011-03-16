#!/bin/bash
# Builds the software components, uploads them to sprint server 
# and places a copy of them on the sprint server ready for installation. 

if [ $# -lt 1 ]; then
    echo "Usage: $0 <sprint number> [minor]"
    exit 1
fi

TODAY=`date "+%Y-%m-%d"`
VER=$1
SUBVER=0
if [ $2 ]; then
	SUBVER=$2
fi
FULL_VER=S$VER.$SUBVER
SPRINT_SERVER=sprint-openbis.ethz.ch
SPRINT_INSTALL_SCRIPT=sprint_install.sh
# this folder is publicly accessible at http://svncisd.ethz.ch/doc/javadoc/
JAVADOC_FOLDER=~/fileserver/doc/javadoc

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
		cp -p *.zip $SPRINT_DIR/
		chmod g+w -R $SPRINT_DIR
	fi
	state_end
}

function copy_to_sprint_server {
	state_start "Copying new openBIS components to '$SPRINT_SERVER'..."
	
	if [ $EXECUTE_COMMANDS ]; then
		scp openBIS-server-S*.zip $SPRINT_SERVER:.
		scp datastore_server-S*.zip $SPRINT_SERVER:.
		scp *.zip $SPRINT_SERVER:~/sprint_builds
		rm -f *.zip
	fi
	state_end
}


function publish_javadocs {

	state_start "Publishing javadocs ..."
	
	if [ $EXECUTE_COMMANDS ]; then
	    pushd .
	    
	    cp -R tmp/openbis_all/targets/dist/javadoc $JAVADOC_FOLDER/$FULL_VER
	    cd $JAVADOC_FOLDER
	    rm -f current
	    ln -s $FULL_VER current
	    
	    popd
	fi
	state_end
}

function install_sprint {
	# If sprint install script is present and executable, run it!
	if [ $EXECUTE_COMMANDS ]; then
		if [ -x $SPRINT_INSTALL_SCRIPT ]; then
			state_start "Installing new sprint builds on '$SPRINT_SERVER'..."
		    echo Installing server remotely...
		    cat $SPRINT_INSTALL_SCRIPT | ssh -T $SPRINT_SERVER "cat > ~/$SPRINT_INSTALL_SCRIPT ; chmod 755 ~/$SPRINT_INSTALL_SCRIPT ; ~/$SPRINT_INSTALL_SCRIPT $VER ; rm -f ~/$SPRINT_INSTALL_SCRIPT"
			state_end
		fi
	fi
}

if [ $EXECUTE_COMMANDS ]; then
	echo -n
else
	state_start "RUNNING DRY RUN"
fi

setup
build
copy_to_cisd_server
copy_to_sprint_server
publish_javadocs
install_sprint

state_start Done!
