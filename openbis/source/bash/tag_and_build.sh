#!/bin/bash
# This script assumes that you have a SSH access on 'sprint-openbis.ethz.ch' 
# and 'cisd-vesuvio.ethz.ch'. This is typically configured in the SSH config file.

if [ $# -lt 1 ]; then
    echo "Usage: $0 <sprint number>"
    exit 1
fi

TODAY=`date "+%Y-%m-%d"`
VER=$1
FULL_VER=S$VER.0
SPRINT_SERVER=sprint-openbis.ethz.ch
CISD_SERVER=cisd-vesuvio.ethz.ch

svn checkout svn+ssh://source.systemsx.ch/repos/cisd/build_resources/trunk build_resources
cd build_resources
echo Tagging openBIS to S$VER...
./tag_sprint.sh openbis S$VER
echo Building openBIS...
./build.sh openbis S$VER

echo Copying new openBIS components to '$CISD_SERVER'... 
OPENBIS_PATH=/localhome/cisd/sprint_builds/openBIS
SPRINT_DIR=$OPENBIS_PATH/$TODAY-$FULL_VER
echo "mkdir -p $SPRINT_DIR"  | ssh -T hal
scp *.zip $CISD_SERVER:$SPRINT_DIR

echo Copying new openBIS components to '$SPRINT_SERVER'... 
scp openBIS-server-*.zip $SPRINT_SERVER:.
scp download-server-*.zip $SPRINT_SERVER:.

# if [ -x sprint_install.sh ]
#    echo Installing server remotely...
#    cat sprint_install.sh | ssh -T $SPRINT_SERVER
# fi

echo Done!
