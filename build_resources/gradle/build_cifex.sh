#!/bin/bash

PRODUCT=$1
BRANCH=$2
VERSION=$3

PROJECTS="\
gradle \
authentication \
common \
dbmigration \
cifex\
"
LATEST_SPRINT=`svn list svn+ssh://svncisd.ethz.ch/repos/cisd/cifex/branches/sprint/|awk '{print $0, length()}'|sort -n --key=2|awk '{print $1'}|tail -1|sed -e "s/.x\///g"`

rm -rf /tmp/gradle-build
mkdir -p /tmp/gradle-build

for project in $PROJECTS; do

	if [[ -z "$BRANCH" ]]
	then
		BRANCH=trunk
	fi

	if [ "$BRANCH" == "trunk" ]
	then
		SVN_PATH="${project}/trunk"
	elif [ "$BRANCH" == "sprint" ]
	then
		if [[ -z "$VERSION" ]]
		then
			VERSION=$LATEST_SPRINT
		fi
		
		if [[ $VERSION == *.* ]]
		then
			TAG=${VERSION}/
			VERSION=`echo $VERSION|cut -d. -f1`			
		else
			TAG=`svn list svn+ssh://svncisd.ethz.ch/repos/cisd/cifex/tags/sprint/${VERSION}.x/|awk '{print $0, length()}'|sort -n --key=2|awk '{print $1'}|tail -1`
		fi
		
		SVN_PATH="cifex/tags/sprint/${VERSION}.x/${TAG}${project}"
		
		svn ls svn+ssh://svncisd.ethz.ch/repos/cisd/$SVN_PATH --depth=empty 2>/dev/null >/dev/null 
		if [ $? -ne 0 ]
		then
			echo "Unable to read svn+ssh://svncisd.ethz.ch/repos/cisd/$SVN_PATH - aborting"
			exit 1
		fi
	else
		echo "Unknown branch $BRANCH (valid options: trunk / sprint)"
		exit 1
	fi

	svn co svn+ssh://svncisd.ethz.ch/repos/cisd/$SVN_PATH /tmp/gradle-build/${project}
done

pushd .
cd /tmp/gradle-build/cifex

./gradlew build -x test
popd
mv /tmp/gradle-build/cifex/targets/gradle/distributions/cifex-*.zip .


