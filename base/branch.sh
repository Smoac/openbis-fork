#!/bin/bash

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 2>/dev/null
if [ $? -eq 0 ]; then echo "Branch already exists!"; exit 1; fi

CURRENT=`svn info|grep URL|cut -d" " -f2`

svn copy $CURRENT svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 -m "create branch $1"
mkdir -p out
rm -r out/temp_checkout
svn checkout --depth=empty svn+ssh://svncisd.ethz.ch/repos/cisd/base/branches/$1 out/temp_checkout
cd out/temp_checkout
svn update gradlew gradle build.gradle
./gradlew dependencyReport
cat out/reports/project/dependencies.txt|egrep ^.---|grep \>|sort|uniq|awk '{print $2 ":" $4}'|awk -F: '{print "s/" $1 ":" $2 ":" $3 "/" $1 ":" $2 ":" $4 "/g"}' > sed_commands
sed -f sed_commands build.gradle > build.gradle.tmp
mv build.gradle.tmp build.gradle
svn commit build.gradle -m "fixed dependencies for branch $1"
cd ../..
