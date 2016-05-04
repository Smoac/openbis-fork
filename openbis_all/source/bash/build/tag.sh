#!/bin/bash


if [ $# -ne 2 ]
then
  echo "Usage: ./tag.sh [branch] [tag]"
  echo ""
  echo "Example: ./tag.sh release/13.04.x 13.04.1"
  exit 1
fi
branch=$1
dir=${2%/*}
if [ $dir == $2 ]; then
  dir=$branch
  tag=$1/$2
else
  tag=$2
fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$branch 2>/dev/null
if [ $? -ne 0 ]; then echo "Branch does not exist!"; exit 1; fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$tag 2>/dev/null
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$dir -m "create tag $tag"
svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$branch svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$tag -m "create tag $tag"

rm -rf elntemp
svn co --depth=immediates svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$tag/openbis_standard_technologies/dist/core-plugins/eln-lims/1/as/webapps/eln-lims/html elntemp
svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$tag|grep Path|awk -F: '{print $2}'|sed -e 's/ //g' > elntemp/version.txt
svn add elntemp/version.txt
svn commit elntemp -m "Added ELN build info to tag $tag"