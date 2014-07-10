#!/bin/bash

if [ $# -lt 2 ]; then
  echo
  echo "Usage: $0 <project name> <branch> [<version>[.<patch number>]]"
  echo
  echo "Creates a tagged version of the specified project."
  echo "If necessary a branch is created first. It will contain the project itself, all its sub-projects"
  echo "as defined in file 'settings.gradle' of the project as well as 'building_resources' and 'gradle'."
  echo
  echo "Currently only the sprint branch is supported."
  echo "If <patch number> is undefined the next available patch number is used."
  echo "If <version> is undefined the next available version number is used."
  echo
  echo "NOTE: Sprint versions have to start with an 'S'."
  echo
  echo "Examples:"
  echo "$0 datamover sprint"
  echo "$0 datamover sprint S184"
  echo "$0 datamover sprint S184.2"
  exit 1
fi

PROJECT=$1
BRANCH=$2
VERSION_TAG=$3

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/utilities.sh

assert_valid_project $PROJECT
if [[ "$BRANCH" == "sprint" ]]; then
  assert_valid_sprint_version_tag $VERSION_TAG
  version=$(get_sprint_version $PROJECT ${BASH_REMATCH[2]})
  patch=$(get_sprint_patch $PROJECT $version ${BASH_REMATCH[4]})
  echo "sprint version number:$version, patch number:$patch"
  create_sprint_branch_if_necessary $PROJECT $version
  copy_sprint_branch_to_tag $PROJECT $version $patch
else
  echo "Branch '$BRANCH' unknown or not supported."
  exit 1
fi