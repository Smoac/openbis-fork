#!/bin/bash
# Upgrades openbis AS and DSS. 
# Assumes that openbis is installed in the 'servers' directory on the same level as this script parent directory

#
# Upgrades the installation from an installer tarball
#
upgrade_from_installer_tarball() 
{

OPENBIS_INSTALL_DIR=$BASE/..
TARBALL=$1
INSTALLER_DIR=${TARBALL%.tar.gz}

mkdir -p $OPENBIS_INSTALL_DIR/backup

pushd $ROOT_DIR > /dev/null

echo "Extracting installation tarball $TARBALL.."
tar xzf $TARBALL

popd > /dev/null

# set INSTALL_PATH in the console.properties
sed_params='s#^INSTALL_PATH=.*$#INSTALL_PATH='$OPENBIS_INSTALL_DIR'#'
sed "$sed_params" $INSTALLER_DIR/console.properties > $INSTALLER_DIR/console.properties.modified
mv $INSTALLER_DIR/console.properties.modified $INSTALLER_DIR/console.properties 

# run the installation
$INSTALLER_DIR/run-console.sh
rm -rf $INSTALLER_DIR

BACKUP_DIR_NAME=`ls -rt $OPENBIS_INSTALL_DIR/backup | tail -1`
BACKUP_DIR=$OPENBIS_INSTALL_DIR/backup/$BACKUP_DIR_NAME

echo "Moving $TARBALL to $BACKUP_DIR..."
mv $TARBALL $BACKUP_DIR

}

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

source $BASE/env
source $BASE/common-functions.sh

ROOT_DIR=$BASE/../servers

$BASE/alldown.sh

installer_tarball=""
for installer_tarball in $ROOT_DIR/*.tar.gz; do
  : # only use the loop to assign the correct value of the installer_tarball
done

if [ -f "$installer_tarball" ]; then
  upgrade_from_installer_tarball $installer_tarball
else
  echo "No installer tarball has been found under $ROOT_DIR. Aborting..."
  exit 1
fi


$BASE/allup.sh