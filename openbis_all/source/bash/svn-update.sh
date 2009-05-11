#!/bin/bash
# Author: Tomasz Pylak
# this small script updates all scripts available in this directory from svn

SVN=http://svncisd.ethz.ch/repos/cisd/openbis_all/trunk/source/bash
TMP=svn-update-tmp
mkdir $TMP
echo Updating the scripts from $SVN
for f in *.sh; do
  if [ "$f" != "$0" ]; then  
    echo Synchronizing $f...
    mv $f $TMP
    wget $SVN/$f
  fi
done
chmod 700 *.sh
rm -fr $TMP
echo SVN update done.