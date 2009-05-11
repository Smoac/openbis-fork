#!/bin/bash
# Author: Tomasz Pylak
# Updates all scripts available in the current directory to the version found in SVN.

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