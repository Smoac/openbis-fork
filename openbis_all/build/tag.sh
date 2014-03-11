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

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 2>/dev/null
if [ $? -ne 0 ]; then echo "Branch does not exist!"; exit 1; fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 2>/dev/null
if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1 -m "create tag folders $1/$2"
svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 -m "create tag $1/$2"

rm -rf out
mkdir -p out

svn checkout --depth=immediates svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 out;

GRADLE_PROJECTS="\
gradle \
authentication \
common \
datamover \
datastore_server \
dbmigration \
deep_sequencing_unit \
installation \
js-test \
openbis \
openbis-common \
openbis_standard_technologies \
openbis_api \
plasmid \
rtd_phosphonetx \
rtd_yeastx \
rtd_cina \
screening \
ui-test\
"

for project in $GRADLE_PROJECTS; do
	cd out/$project;
	svn update gradlew gradle build.gradle settings.gradle javaproject.gradle repository.gradle gwtdev.gradle query-api.gradle proteomics-api.gradle screening-api.gradle admin-console.gradle clients.gradle;
	cd ../..;
done

for project in $GRADLE_PROJECTS; do
	cd out/$project;
	./gradlew dependencyReport;
	cat targets/gradle/reports/project/dependencies.txt|egrep ^.---|grep \>|sort|uniq|awk '{print $2 ":" $4}'|awk -F: '{print "s/" $1 ":" $2 ":" $3 "/" $1 ":" $2 ":" $4 "/g"}' > sed_commands;
	
	for file in build.gradle javaproject.gradle gwtdev.gradle query-api.gradle proteomics-api.gradle screening-api.gradle admin-console.gradle clients.gradle; do
		if [ -s $file ]; then
			sed -f sed_commands $file > $file.tmp;
			mv $file.tmp $file;	
		fi;
	done
	
	rm sed_commands
	svn commit build.gradle -m "fixed dependencies for $1/$2";
	cd ../..;
done
