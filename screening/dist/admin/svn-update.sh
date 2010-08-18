#!/bin/bash
# Author: Tomasz Pylak
# Updates all the screening admin scripts to the version found in SVN.

unalias rm

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

SVN=svncisd.ethz.ch/repos/cisd/screening/trunk/dist/admin
cd $BASE
wget -nv -r -l1 -A.sh http://$SVN/
mv $SVN/* .
chmod 700 *.sh
RM=`echo $SVN | cut -d / -f1`
rm -rf $RM
