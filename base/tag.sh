#!/bin/bash

if [ `dirname $0` != "." ]
then
	echo "Please run from the same directory than the script source file is in"
	exit 1
fi

if [ $# -ne 2 ]
then
  echo "Usage: ./tag.sh [branch] [tag]"
  echo ""
  echo "Example: ./tag.sh release/13.04.x 13.04.1"
  exit 1
fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 2>/dev/null
if [ $? -ne 0 ]; then echo "Branch does not exist!"; exit 1; fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 2>/dev/null
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1 -m "create tag folders $1/$2"
svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 -m "create tag $1/$2"
