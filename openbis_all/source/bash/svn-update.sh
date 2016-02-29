#!/bin/bash
# Author: Tomasz Pylak
# Updates all scripts available in the current directory and its subdirectories to the version found in SVN.

SVN=svnsis.ethz.ch/repos/cisd/openbis_all/trunk/source/bash
cd ~openbis/bin
rm -r build
wget -nv -r -l2 -A.{sh,txt} http://$SVN/
mv $SVN/* .
chmod 700 *.sh
chmod 700 build/*.sh
RM=`echo $SVN | cut -d / -f1`
rm -rf $RM
