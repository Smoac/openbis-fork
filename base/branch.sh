#!/bin/bash

if [ `dirname $0` != "." ]
then
	echo "Please run from the same directory than the script source file is in"
	exit 1
fi

if [ $# -ne 1 ]
then
  echo "Usage: ./branch.sh [branch]"
  echo ""
  echo "Example: ./branch.sh release/13.04.x"
  exit 1
fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 2>/dev/null
if [ $? -eq 0 ]; then echo "Branch already exists!"; exit 1; fi

CURRENT=`svn info|grep URL|cut -d" " -f2`

svn copy $CURRENT svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 -m "create branch $1"
mkdir -p out
rm -r out/temp_checkout
svn checkout --depth=empty svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 out/temp_checkout
if [ $? -ne 0 ]; then echo "Checkout of new branch $1 failed!"; exit 1; fi

cd out/temp_checkout
svn update gradlew gradle build.gradle
./gradlew dependencyReport
cat out/reports/project/dependencies.txt|egrep ^.---|grep \>|sort|uniq|awk '{print $2 ":" $4}'|awk -F: '{print "s/" $1 ":" $2 ":" $3 "/" $1 ":" $2 ":" $4 "/g"}' > sed_commands
sed -f sed_commands build.gradle > build.gradle.tmp
mv build.gradle.tmp build.gradle
svn commit build.gradle -m "fixed dependencies for branch $1"
cd ../..
