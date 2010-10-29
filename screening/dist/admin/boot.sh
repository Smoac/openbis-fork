#!/bin/bash
# Fetches "openBIS for HCS" administration scripts from source repository to the bin folder in current directory.

SVN_DIR=svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin

mkdir bin
cd bin
wget $SVN_DIR/svn-update.sh
wget $SVN_DIR/env
wget $SVN_DIR/empty-screening-database.sql
. ./svn-update.sh
cd ..