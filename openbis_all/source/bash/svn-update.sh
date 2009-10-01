#!/bin/bash
# Author: Tomasz Pylak
# Updates all scripts available in the current directory to the version found in SVN.

SVN=svncisd.ethz.ch/repos/cisd/openbis_all/trunk/source/bash
cd ~/bin
wget -nv -r -l1 -A.sh http://$SVN/
mv $SVN/* .
chmod 700 *.sh
RM=`echo $SVN | cut -d / -f1`
rm -rf $RM
