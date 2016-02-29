#!/bin/bash
# Fetches "openBIS for HCS" administration scripts from source repository to the bin folder in current directory.

SVN_DIR=svnsis.ethz.ch/repos/cisd/screening/trunk/dist/admin

mkdir bin
cd bin
wget $SVN_DIR/svn-update.sh
wget $SVN_DIR/env
. ./svn-update.sh
cd ..