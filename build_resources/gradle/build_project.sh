#!/bin/bash

IVY_REPO=""
if [ "$1" == "--publish" ]; then
  IVY_REPO="$2"
  shift 2
fi


PROJECT=$1
BRANCH=$2
VERSION=$3

BUILDING_SITE="/tmp/gradle-build"

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
source $BASE/utilities.sh

template_url=$(calculate_repository_template_url $PROJECT $BRANCH $VERSION)
echo "Repository Template URL: $template_url"
if [[ -z "$template_url" ]]; then
  exit 1
fi

rm -rf "$BUILDING_SITE"


checkout() 
{
  local project=$1
  
  echo svn co ${template_url/__project__/$project} -q "$BUILDING_SITE/$project"
  svn co ${template_url/__project__/$project} -q "$BUILDING_SITE/$project"
}

#
# Retrieve main project
#
checkout $PROJECT
settings_gradle_file="$BUILDING_SITE/$PROJECT/settings.gradle"
if [ ! -f "$settings_gradle_file" ]; then
  echo "Building stopped because no file settings.gradle found in ${TEMPLATE_URL/__project__/$PROJECT}."
  exit 1
fi

projects=`awk '/includeFlat/ {line=substr($0,12); while(match(line,",[\t ]*$")) {getline;line=(line $0)}; gsub(",", " ", line); gsub("'\''", "", line); print line}' "$settings_gradle_file"`
for project in $projects; do
  if [[ $project != $PROJECT ]]; then
    checkout $project
  fi
done
checkout gradle

pushd .
target=build
if [ "$IVY_REPO" != "" ]; then
  target=publish
  if [ ${IVY_REPO:0:1} != "/" ]; then
    IVY_REPO="$PWD/$IVY_REPO"
  fi
fi
cd "$BUILDING_SITE/$PROJECT"
./gradlew --gradle-user-home "$BUILDING_SITE" $target -x test -PivyRepository="$IVY_REPO"
popd
if [ -d "$BUILDING_SITE/$PROJECT/targets/gradle/distributions" ]; then
  for f in "$BUILDING_SITE/$PROJECT/targets/gradle/distributions/*"; do
    mv $f .
  done
fi


