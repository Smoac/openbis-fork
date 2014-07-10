#!/bin/bash

if [ $# -lt 2 ]; then
  echo
  echo "Usage: $0 <project name> <branch> [<version>[.<patch number>]]"
  echo
  echo "Creates a tagged version of the specified project. If necessary a branch is created first."
  echo "It will contain the project itself, all its sub-projects"
  echo "as defined in file 'settings.gradle' of the project as well as 'building_resources' and 'gradle'."
  echo
  echo "Currently only the sprint and release branched can be tagged."
  echo "If <patch number> is undefined the next available patch number is used."
  echo "If <version> is undefined the next available version number is used in case of sprint branch."
  echo -n "In case of release branch the current date is used to create a version of "
  echo "the form <2-digit year>.<2-digit month>."
  echo
  echo "NOTE: Sprint versions have to start with an 'S'."
  echo
  echo "Examples:"
  echo "$0 datamover sprint"
  echo "$0 datamover sprint S184"
  echo "$0 datamover sprint S184.2"
  echo "$0 datamover release"
  echo "$0 datamover release 10.06"
  echo "$0 datamover release 10.06.13"
  exit 1
fi

PROJECT=$1
BRANCH=$2
VERSION_TAG=$3

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/gradle/utilities.sh

assert_valid_project $PROJECT
if [[ "$BRANCH" == "sprint" ]]; then
  assert_valid_sprint_version_tag $VERSION_TAG
  version=S$(get_sprint_version $PROJECT ${BASH_REMATCH[2]})
elif [[ "$BRANCH" == "release" ]]; then
  assert_valid_release_version_tag $VERSION_TAG
  version=$(get_release_version ${BASH_REMATCH[2]})
else
  echo "Tagging of branch '$BRANCH' not supported."
  exit 1
fi
patch=$(get_patch_number $PROJECT $BRANCH $version ${BASH_REMATCH[4]})
echo "$BRANCH version:$version, patch number:$patch"
create_branch_if_necessary $PROJECT $BRANCH $version
copy_branch_to_tag $PROJECT $BRANCH $version $patch