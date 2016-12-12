#!/bin/bash


if [ $# -ne 1 ]
then
  echo "Usage: ./branch.sh [branch]"
  echo ""
  echo "Example: ./branch.sh release/13.04.x"
  exit 1
fi

svn info svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 2>/dev/null
if [ $? -eq 0 ]; then echo "Branch already exists!"; exit 1; fi


ALL_PROJECTS="\
gradle \
authentication \
build_resources \
commonbase \
common \
datamover \
datastore_server \
dbmigration \
deep_sequencing_unit \
eu_agronomics \
eu_basynthec \
eu_basysbio \
image_readers \
installation \
integration-tests \
js-test \
libraries \
openbis \
openbis_all \
openbis_api \
openbis_mobile \
openbis_oai_pmh \
openbis_standard_technologies \
openbis-common \
plasmid \
rtd_cina \
rtd_phosphonetx \
rtd_yeastx \
sanofi \
screening \
ui-test\
"

rm -rf tmp

svn mkdir --parents svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 -m "create branch $1";
svn co svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 tmp

for project in $ALL_PROJECTS; do
	svn copy svn+ssh://svncisd.ethz.ch/repos/cisd/$project/trunk tmp/$project
done

cd tmp
svn commit -m "create branch $1"
cd ..

rm -rf tmp
mkdir -p tmp

svn checkout --depth=immediates svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/$1 tmp;

GRADLE_PROJECTS="\
authentication \
commonbase \
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

for project in $GRADLE_PROJECTS gradle; do
	cd tmp/$project;
	svn update gradlew gradle build.gradle settings.gradle javaproject.gradle repository.gradle gwtdev.gradle query-api.gradle proteomics-api.gradle screening-api.gradle admin-console.gradle clients.gradle;
	cd ../..;
done

for project in $GRADLE_PROJECTS; do
	cd tmp/$project;
	./gradlew dependencyReport;
	cat targets/gradle/reports/project/dependencies.txt|egrep ^.---|grep \>|sort|uniq|awk '{print $2 ":" $4}'|awk -F: '{print "s/" $1 ":" $2 ":" $3 "/" $1 ":" $2 ":" $4 "/g"}' > sed_commands;
	
	for file in build.gradle javaproject.gradle gwtdev.gradle query-api.gradle proteomics-api.gradle screening-api.gradle admin-console.gradle clients.gradle; do
		if [ -s $file ]; then
			sed -f sed_commands $file > $file.tmp;
			mv $file.tmp $file;	
		fi;
	done
	
	rm sed_commands
	cd ../..;
done

cd tmp
svn commit -m "fixed dependencies of $1";
cd ..
