#!/bin/bash

#	
# reads the 'admin' user password from the console
#
function readAdminPassword()
{
	if [ -z "$ADMIN_PASSWORD" ]; then
	    read -s -p "Enter password for openBIS 'admin' user : " ADMIN_PASSWORD
	    echo ""
	    
	    read -s -p "Re-type password for openBIS 'admin' user : " ADMIN_PASSWORD2
	    echo ""
	    
	    if [ "$ADMIN_PASSWORD" != "$ADMIN_PASSWORD2" ]; then
	        echo "Administrator passwords do not match. Aborting installation."
	        exit 2
	    fi
	fi
}

#
# enforces the availability of a certain command on the system path
#
function ensureToolOnPath() 
{
  cmd=$1
  cmdLocation=$(which $cmd)
  
  if [ -z "$cmdLocation" ]; then
  
     echo "The installation process requires '$cmd' to be on the system path."
     echo "Please set the PATH variable accordingly and try again."
     exit 3
  
  fi
}


BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ensureToolOnPath "java" 

install_path=$( grep -e "^INSTALL_PATH=.*$" $BASE/console.properties | sed "s/INSTALL_PATH=//" )
if [ -z "$install_path" ]; then
    echo "The property INSTALL_PATH must be configured in $BASE/console.properties."
    echo "Please edit the file and run the installation script again."
    exit 1
fi
	
if [ -d "$install_path" ]; then
  echo "Previous openBIS installation detected. Upgrading..."
else 
  dss_root_dir=$( grep -e "^DSS_ROOT_DIR=.*$" $BASE/console.properties | sed "s/DSS_ROOT_DIR=//" )
  if [ -z "$dss_root_dir" ]; then
      echo "The property DSS_ROOT_DIR must be configured in $BASE/console.properties."
      echo "Please edit the file and run the installation script again."
      exit 1
  fi
  readAdminPassword
fi

java -Djava.util.logging.config.file=$BASE/jul.config -DADMIN_PASSWORD=$ADMIN_PASSWORD -DCONSOLE=true -Dmerge.props.to.installation.vars=true -jar $BASE/openBIS-installer.jar -options-auto $BASE/console.properties
