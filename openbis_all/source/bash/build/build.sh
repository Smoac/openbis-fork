#!/bin/sh
usage()
{
  echo ""
  echo "Usage: ./build.sh version branch/tag"
  echo ""
  echo "Example: ./build.sh sprint S175.x/S175.0"
  exit 1
}

function move_to_file_server {
  echo "Moving new openBIS components to file server"
  
  OPENBIS_PATH=~openbis/fileserver/sprint_builds/openBIS
  SPRINT_DIR=$OPENBIS_PATH/$TODAY-$FULL_VER
  mkdir -p $SPRINT_DIR
  mv *$FULL_VER*.{zip,gz} $SPRINT_DIR/
  chmod g+w -R $SPRINT_DIR
}

if [ $# -ne 2 ]
then
	usage
fi

TODAY=`date "+%Y-%m-%d"`
FULL_VER=$2

rm -rf tmp
mkdir -p tmp
svn checkout svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 tmp;
if [ $? -ne 0 ]
then
	exit
fi

cd tmp/openbis_standard_technologies
./gradlew :clientsAndApis -x test
./gradlew :generateJavadoc
cd ../installation
./gradlew :build -x test
cd ../plasmid
./gradlew :build -x test
cd ../..
mv tmp/openbis_standard_technologies/targets/gradle/distributions/openBIS-clients-and-APIs*.zip .
mv tmp/installation/targets/gradle/distributions/openBIS-installation-standard-technologies*.tar.gz .
mv tmp/plasmid/targets/gradle/distributions/datastore_server_plugin-plasmid*.zip .
cp -r tmp/openbis_standard_technologies/targets/gradle/docs/javadoc ~openbis/fileserver/doc/openbis/$FULL_VER
cd ~openbis/fileserver/doc/openbis
if [ ${FULL_VER:0:1} == "S" ]; then
  rm current
  ln -s $FULL_VER current
else
  dir=${FULL_VER%.*}
  rm $dir
  ln -s $FULL_VER $dir
fi
cd -

move_to_file_server

