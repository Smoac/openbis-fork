#!/bin/bash


if [ $# -ne 2 ]
then
  echo "Usage: ./tag.sh [branch] [tag]"
  echo ""
  echo "Example: ./tag.sh release/13.04.x 13.04.1"
  exit 1
fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 2>/dev/null
if [ $? -ne 0 ]; then echo "Branch does not exist!"; exit 1; fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 2>/dev/null
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1 -m "create tag $1/$2"
svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 -m "create tag $1/$2"

rm -rf elntemp
svn co --depth=immediates svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2/plasmid/source/core-plugins/newbrowser elntemp
svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2|grep Path|awk -F: '{print $2}'|sed -e 's/ //g' > elntemp/version.txt
svn add elntemp/version.txt
svn commit elntemp -m "Added ELN build info to tag $1/$2"