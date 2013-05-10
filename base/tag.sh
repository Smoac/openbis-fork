#!/bin/bash
svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 2>/dev/null
if [ $? -neq 0 ]; then echo "Branch does not exist!"; exit 1; fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 2>/dev/null
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 -m "create tag $1/$2"
mkdir -p out
rm -r out/temp_checkout
svn checkout --depth=empty svn+ssh://svncisd.ethz.ch/repos/cisd/base/tags/$1/$2 out/temp_checkout
cd out/temp_checkout
svn update build.gradle
sed -e "s/UNSPECIFIED_VERSION/$2/g" build.gradle > build.gradle.tmp
mv build.gradle.tmp build.gradle
svn commit build.gradle -m "fixed version for tag $1/$2"
cd ../..
