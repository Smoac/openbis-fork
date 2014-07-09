#!/bin/bash

PROJECT=$1
BRANCH=$2
VERSION_TAG=$3

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/utilities.sh

if [[ "$BRANCH" == "sprint" ]]; then
  version=$(get_sprint_version $PROJECT $VERSION_TAG)
  echo "sprint version:$version"
  create_sprint_branch_if_necessary $PROJECT $version
else
  echo "Branch '$BRANCH' unknown or not supported."
  exit 1
fi