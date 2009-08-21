#!/bin/bash
# Author: Tomasz Pylak
# Updates all scripts available in the current directory to the version found in SVN.

SVN=svncisd.ethz.ch/repos/cisd/openbis_all/trunk/source/bash
TMP=svn-update-tmp
mkdir $TMP
wget -r -l1 -P$TMP -A.sh http://$SVN/
mv $TMP/$SVN/* .
chmod 700 *.sh
rm -fr $TMP
