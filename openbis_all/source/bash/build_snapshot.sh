#!/bin/bash
# Builds snapshot distributions of the software components, uploads them to sprint server.
#
# This script assumes that you have a SSH access on 'sprint-openbis.ethz.ch'. 
# This is typically configured in the SSH config file.

SPRINT_SERVER=sprint-openbis.ethz.ch
SPRINT_INSTALL_SCRIPT=sprint_install.sh

svn checkout http://svncisd.ethz.ch/repos/cisd/build_resources/trunk build_resources
cd build_resources
echo Building openBIS...
./build.sh --private openbis_all

echo Copying new openBIS components to \'$SPRINT_SERVER\'...
scp openBIS-server-*.zip $SPRINT_SERVER:.
scp datastore_server-*.zip $SPRINT_SERVER:.
rm -f *.zip

# If sprint install script is present and executable, run it!
if [ -x $SPRINT_INSTALL_SCRIPT ]; then
    echo Installing server remotely...
    cat $SPRINT_INSTALL_SCRIPT | ssh -T $SPRINT_SERVER "cat > ~/$SPRINT_INSTALL_SCRIPT ; chmod 755 ~/$SPRINT_INSTALL_SCRIPT ; ~/$SPRINT_INSTALL_SCRIPT ; rm -f ~/$SPRINT_INSTALL_SCRIPT"
fi
echo Done!