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

pushd $ROOT_DIR > /dev/null

echo "Extracting installation tarball $TARBALL.."
tar xzf $TARBALL

popd > /dev/null

# set INSTALL_PATH in the console.properties
sed_params='s#^INSTALL_PATH=.*$#INSTALL_PATH='$OPENBIS_INSTALL_DIR'#'
sed -i '' "$sed_params" $INSTALLER_DIR/console.properties

# run the installation
$INSTALLER_DIR/run-console.sh

rm -rf $INSTALLER_DIR

echo "Moving $TARBALL to $OPENBIS_INSTALL_DIR/backup ..."
mkdir -p $OPENBIS_INSTALL_DIR/backup
mv $TARBALL $OPENBIS_INSTALL_DIR/backup
}

BASE=`dirname "$0"`
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