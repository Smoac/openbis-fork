#!/bin/bash

PROJECT=$1
BRANCH=$2
VERSION=$3

REPOSITORY_URL="svn+ssh://svncisd.ethz.ch/repos/cisd"
BUILDING_SITE="/tmp/gradle-build"

rm -rf "$BUILDING_SITE"

#
# Calculate repository template URL
#
if [[ -z "$BRANCH" ]]; then
  BRANCH="trunk"
fi
if [ "$BRANCH" == "trunk" ]; then
  TEMPLATE_URL="$REPOSITORY_URL/__project__/trunk"
else
  if [[ "$VERSION" == *.x ]]; then
    TEMPLATE_URL="$REPOSITORY_URL/$PROJECT/branches/$BRANCH/$VERSION/__project__"
  else
    if [ "$BRANCH" == "sprint" ]; then
      if [[ -z "$VERSION" ]]; then
        LATEST_SPRINT=`svn list $REPOSITORY_URL/$PROJECT/branches/sprint/|awk -F. '{print substr($1,2)}'|sort -nr|head -1`
        VERSION=S$LATEST_SPRINT
      fi
      if [[ "$VERSION" == *.* ]]; then
        TAG="$VERSION"
        VERSION=`echo $VERSION|cut -d. -f1`
      else
        LATEST_PATCH=`svn list $REPOSITORY_URL/$PROJECT/tags/sprint/$VERSION.x/|awk -F. '{print $2}'|sort -nr|head -1`
        TAG=$VERSION.${LATEST_PATCH%/}
      fi
      TEMPLATE_URL="$REPOSITORY_URL/$PROJECT/tags/$BRANCH/$VERSION.x/$TAG/__project__"
    elif [[ -z "$VERSION" ]]; then
      echo "Missing version argument. Needed for branch '$BRANCH'."
      exit 1
    else
      if [[ "$VERSION" == *.*.* ]]; then
        TAG="$VERSION"
        VERSION=`echo $VERSION|cut -d. -f1,2`
      else
        LATEST_PATCH=`svn list $REPOSITORY_URL/$PROJECT/tags/$BRANCH/$VERSION.x/|awk -F. '{print $3}'|sort -nr|head -1`
        TAG=$VERSION.${LATEST_PATCH%/}
      fi
      TEMPLATE_URL="$REPOSITORY_URL/$PROJECT/tags/$BRANCH/$VERSION.x/$TAG/__project__"
    fi
  fi
fi
echo "Repository Template URL: $TEMPLATE_URL"

#
# Retrieve main project
#
echo svn co ${TEMPLATE_URL/__project__/$PROJECT} -q "$BUILDING_SITE/$PROJECT"
svn co ${TEMPLATE_URL/__project__/$PROJECT} -q "$BUILDING_SITE/$PROJECT"
settings_gradle_file="$BUILDING_SITE/$PROJECT/settings.gradle"
if [ ! -f "$settings_gradle_file" ]; then
  echo "Building stopped because no file settings.gradle found in ${TEMPLATE_URL/__project__/$PROJECT}."
  exit 1
fi

projects=`awk '/includeFlat/ {line=substr($0,12); while(match(line,", *$")) {getline;line=(line $0)}; gsub(", *", "\n", line); gsub(" *'\''", "", line); print line}' "$settings_gradle_file"`
for project in $projects; do
  if [[ $project != $PROJECT ]]; then
    echo svn co ${TEMPLATE_URL/__project__/$project} -q "$BUILDING_SITE/$project"
    svn co ${TEMPLATE_URL/__project__/$project} -q "$BUILDING_SITE/$project"
  fi
done

pushd .
cd "$BUILDING_SITE/$PROJECT"
./gradlew build -x test
popd
mv "$BUILDING_SITE/$PROJECT/targets/gradle/distributions/*" .

 

#rm -rf "$BUILDING_SITE"


