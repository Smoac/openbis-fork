#!/bin/sh
usage()
{
  echo ""
  echo "Usage: ./build.sh [--publish <local ivy repository>] openbis|apis|<project> [trunk|sprint] [<sprint version>]"
  echo ""
  echo "Example: ./build.sh cifex" - will build cifex installer from trunk
  echo "Example: ./build.sh apis sprint" - will build openBIS clients and APIs package from latest released sprint.
  echo "Example: ./build.sh openbis sprint S178" - will build the openBIS installer from latest revision of sprint S178.
  echo "Example: ./build.sh apis sprint S178.1" - will build openBIS clients and APIs package from revision 1 of sprint S178. 
  echo "Example: ./build.sh --publish ~/dev/eclipse-workspace/ivy-repository base sprint" - will build base from latest sprint release and publish it. 
  echo ""
  echo "NOTE:"
  echo " 1. This script can be used to build openBIS starting from S178. Earlier versions need to be built with ./build_ant.sh"
  echo " 2. The publish option is currently not supported for openbis and apis"
  exit 1
}

if [ $# -lt 1 ]; then
  usage
fi

DIR=`dirname $0`

if [ "$1" == "--publish" ]; then
  if [ $# -lt 3 ]; then
    usage
  fi
  project=$3
else
  project=$1
fi

if [ "$project" == "openbis" ] || [ "$project" == "apis" ]; then
  if [ "$1" == "--publish" ]; then
    shift 2
  fi
  ${DIR}/gradle/build_openbis.sh $@
else
  ${DIR}/gradle/build_project.sh $@
fi

exit 0
