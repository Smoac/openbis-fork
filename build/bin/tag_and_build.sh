#!/bin/bash
BIN_DIR=`dirname "$0"`

usage()
{
 	echo ""
 	echo "Usage: ./tag_and_build.sh branch release_major_version release_minor_version"
 	echo "For Sprints"
 	echo "Example: ./tag_and_build.sh master 175 1"
 	echo "For Releases"
 	echo "Example: ./tag_and_build.sh 6.5.x 6.5 0"
 	exit 1
}

# parameter checks

if [ $# -eq 0 ]
then
	usage
fi

BRANCH=$1
VER=$2
SUBVER=0
if [ $3 ]; then
	SUBVER=$3
fi
FULL_VER=$VER.$SUBVER

"$BIN_DIR/tag.sh" $BRANCH $FULL_VER
if [ $? -ne 0 ];then exit 1; fi	

"$BIN_DIR/build.sh" $BRANCH $FULL_VER