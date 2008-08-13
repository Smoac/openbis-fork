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
./tag_sprint.sh openbis S$VER
./build.sh openbis S$VER

OPENBIS_PATH=/localhome/cisd/sprint_builds/openBIS
SPRINT_DIR=$OPENBIS_PATH/$TODAY-$FULL_VER
echo "mkdir -p $SPRINT_DIR"  | ssh -T hal
scp *.zip $CISD_SERVER:$SPRINT_DIR

scp openBIS-server-*.zip $SPRINT_SERVER:.
scp download-server-*.zip $SPRINT_SERVER:.
