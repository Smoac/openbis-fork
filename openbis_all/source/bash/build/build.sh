#!/bin/sh
usage()
{
  echo ""
  echo "Usage: ./build.sh version branch/tag"
  echo ""
  echo "Example: ./build.sh sprint S175.x/S175.0"
  exit 1
}

if [ $# -ne 2 ]
then
	usage
fi

rm -rf tmp
mkdir -p tmp
svn checkout svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/tags/$1/$2 tmp;
if [ $? -ne 0 ]
then
	exit
fi

cd tmp/gradle
./gradlew :openbis_standard_technologies:clientsAndApis
./gradlew :installation:build
cd ../..
mv tmp/openbis_standard_technologies/targets/gradle/distributions/openBIS-clients-and-APIs*.zip .
mv tmp/installation/targets/gradle/distributions/openBIS-installation-standard-technologies*.tar.gz .
