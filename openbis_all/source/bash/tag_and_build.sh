#!/bin/bash
# Tags, builds the software components, uploads them to sprint server 
# and places a copy of them on the sprint server ready for installation. 
#
# This script assumes that you have a SSH access on 'sprint-openbis.ethz.ch' 
# and 'cisd-vesuvio.ethz.ch'. This is typically configured in the SSH config file.

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
CISD_SERVER=cisd-vesuvio.ethz.ch
SPRINT_INSTALL_SCRIPT=sprint_install.sh

svn checkout svn+ssh://svncisd.ethz.ch/repos/cisd/build_resources/trunk build_resources
cd build_resources
echo Tagging openBIS to $FULL_VER...
./tag_sprint.sh openbis_all $FULL_VER
echo Building openBIS...
./build.sh openbis_all $FULL_VER

echo Copying new openBIS components to \'$CISD_SERVER\'...
OPENBIS_PATH=/localhome/cisd/sprint_builds/openBIS
SPRINT_DIR=$OPENBIS_PATH/$TODAY-$FULL_VER
echo "mkdir -p $SPRINT_DIR"  | ssh -T $CISD_SERVER
scp *.zip $CISD_SERVER:$SPRINT_DIR
echo "chmod g+w -R $SPRINT_DIR" | ssh -T $CISD_SERVER

echo Copying new openBIS components to \'$SPRINT_SERVER\'...
scp openBIS-server-S*.zip $SPRINT_SERVER:.
scp datastore_server-S*.zip $SPRINT_SERVER:.
scp *.zip $SPRINT_SERVER:~/sprint_builds
rm -f *.zip

# If sprint install script is present and executable, run it!
if [ -x $SPRINT_INSTALL_SCRIPT ]; then
    echo Installing server remotely...
    cat $SPRINT_INSTALL_SCRIPT | ssh -T $SPRINT_SERVER "cat > ~/$SPRINT_INSTALL_SCRIPT ; chmod 755 ~/$SPRINT_INSTALL_SCRIPT ; ~/$SPRINT_INSTALL_SCRIPT $VER ; rm -f ~/$SPRINT_INSTALL_SCRIPT"
fi

echo Done!