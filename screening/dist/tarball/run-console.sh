#!/bin/bash

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi


install_path_configured=$(grep -e "^INSTALL_PATH=.*\w.*$" $BASE/console.properties)
if [ -z "$install_path_configured" ]; then
    echo "The property INSTALL_PATH must be configured in $BASE/console.properties."
    echo "Please edit the file and run the installation script again."
    exit 1
fi

#
# create 'admin' user
#
if [ -z "$ADMIN_PASSWORD" ]; then
    read -s -p "Enter password for openBIS 'admin' user : " ADMIN_PASSWORD
    echo ""
    
    read -s -p "Re-type password for openBIS 'admin' user : " ADMIN_PASSWORD2
    echo ""
    
    if [ "$ADMIN_PASSWORD" -ne "$ADMIN_PASSWORD2" ]; then
        echo "Administrator passwords do not match. Aborting installation."
        exit 2
    fi
fi


java -Djava.util.logging.config.file=$BASE/jul.config -DADMIN_PASSWORD=$ADMIN_PASSWORD -Dmerge.props.to.installation.vars=true -jar $BASE/openBIS-installer.jar -options-auto $BASE/console.properties