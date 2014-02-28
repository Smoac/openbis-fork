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

#svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 2>/dev/null
#if [ $? -eq 0 ]; then echo "Tag already exists!"; exit 1; fi

#svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1 -m "create tag folders $1/$2"
#svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 -m "create tag $1/$2"

rm -r out
mkdir -p out

svn checkout --depth=immediates svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 out;

for project in gradle authentication common datastore_server dbmigration deep_sequencing_unit installation integration-tests js-test openbis openbis_all openbis_api openbis_knime openbis_mobile openbis_standard_technologies openbis-common plasmid rtd_cina rtd_phosphonetx rtd_yeastx screening ui-test; do
	cd out/$project;
	svn update gradlew gradle build.gradle settings.gradle javaproject.gradle repository.gradle gwtdev.gradle query-api.gradle proteomics-api.gradle screening-api.gradle admin-console.gradle clients.gradle;
	cd ../..;
done

for project in authentication common datastore_Server dbmigration deep_sequencing_unit installation integration-tests js-test openbis openbis_all openbis_api openbis_knime openbis_mobile openbis_standard_technologies openbis-common plasmid rtd_cina rtd_phosphonetx rtd_yeastx screening ui-test; do
	cd out/$project;
	./gradlew dependencyReport;
	cat targets/gradle/reports/project/dependencies.txt|egrep ^.---|grep \>|sort|uniq|awk '{print $2 ":" $4}'|awk -F: '{print "s/" $1 ":" $2 ":" $3 "/" $1 ":" $2 ":" $4 "/g"}' > sed_commands;
	sed -f sed_commands build.gradle > build.gradle.tmp;
	mv build.gradle.tmp build.gradle;
	rm sed_commands
#	svn commit build.gradle -m "fixed dependencies for $1/$2";
	cd ../..;
done

