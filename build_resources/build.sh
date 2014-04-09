#!/bin/sh
usage()
{
  echo ""
  echo "Usage: ./build.sh <openbis/cifex/apis> [trunk/sprint] [sprint version]"
  echo ""
  echo "Example: ./build.sh cifex" - will build cifex installer from trunk
  echo "Example: ./build.sh apis sprint" - will build openBIS clients and APIs package from latest released sprint.
  echo "Example: ./build.sh openbis sprint S178" - will build the openBIS installer from latest revision of sprint S178.
  echo "Example: ./build.sh apis sprint S178.1" - will build openBIS clients and APIs package from revision 1 of sprint S178. 
  echo ""
  echo "NOTE: This script can be used to build openBIS starting from S178. Earlier versions need to be built with ./build_ant.sh"
  exit 1
}

if [ $# -lt 1 ]
then
	usage
fi

DIR=`dirname $0`

if [ "$1" == "openbis" ] || [ "$1" == "apis" ]
then
	${DIR}/gradle/build_openbis.sh $@
elif [ "$1" == "cifex" ]
then
	${DIR}/gradle/build_cifex.sh $@
else
	echo "Unknown product $1"
fi

exit 0
