#!/bin/bash

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/common/branches/$1 2>/dev/null
if [ $? -eq 0 ]; then echo "Branch already exists!"; exit 1; fi

svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/common/trunk svn+ssh://svncisd.ethz.ch/repos/cisd/common/branches/$1 -m "create branch $1"
mkdir -p out
svn checkout --depth=empty svn+ssh://svncisd.ethz.ch/repos/cisd/common/branches/$1 out/common_checkout
cd out/common_checkout
svn update gradlew gradle build.gradle
./gradlew dependencyReport
cat out/reports/project/dependencies.txt|egrep ^.---|awk '{print $2}'|sort|uniq|egrep -v ^$|awk -F: '{print "s/" $1 ":" $2 ":+/" $1 ":" $2 ":" $3 "/g"}' > sed_commands
sed -f sed_commands build.gradle > build.gradle.tmp
mv build.gradle.tmp build.gradle
svn commit build.gradle -m "fixed dependencies for branch $1"
cd ../..
