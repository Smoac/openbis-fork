#!/bin/bash
# This script assumes that 'sprint-openbis' matches the sprint server (hostname: 'sprint-openbis.ethz.ch')
# configured in the SSH config file and that 'hal' matches hostname 'cisd-hal.ethz.ch'.

if [ $# -lt 1 ]; then
    echo "Usage: $0 <sprint number>"
    exit 1
fi

TODAY=`date "+%Y-%m-%d"`
VER=$1
FULL_VER=S$VER.0

svn checkout svn+ssh://source.systemsx.ch/repos/cisd/build_resources/trunk build_resources
cd build_resources
./tag_sprint.sh openbis S$VER
./build.sh openbis S$VER

HAL_PATH=../cisd/sprint_builds/openBIS
HAL_DIR=$HAL_PATH/$TODAY-$FULL_VER
echo "mkdir -p $HAL_DIR"  | ssh -T hal
scp *.zip hal:$HAL_DIR

scp openBIS-server-*.zip sprint-openbis:.
scp download-server-*.zip sprint-openbis:.
