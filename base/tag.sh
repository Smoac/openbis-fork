#!/bin/bash
svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 2>/dev/null
if [ $? -ne 0 ]; then echo "Branch does not exist!"; exit 1; fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 2>/dev/null
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1 -m "create tag folders $1/$2"
svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 -m "create tag $1/$2"
