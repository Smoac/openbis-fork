#!/bin/bash
#
# 1) Mark all scripts as executable 
# 2) Clean up all temporary folders after installation 
# 3) Switch on/off plugins in standard-technologies-applicationContext.xml
#

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

INSTALL_TMPEXTRACT=$1
DATA_TMPEXTRACT=$2
DSS_ROOT_DIR=$3
INSTALL_PATH=$4
DISABLED_TECHNOLOGIES=$5

echo Finish installation...
source $BASE/chmodx-all-scripts.sh


if [ -d "$INSTALL_TMPEXTRACT" ]; then
  rm -rf $INSTALL_TMPEXTRACT
fi

if [ -d "$DATA_TMPEXTRACT" ]; then
   mkdir -p "$DSS_ROOT_DIR"
   for f in `ls "$DATA_TMPEXTRACT/data/"`; do
      cp -Rf "$DATA_TMPEXTRACT/data/$f" "$DSS_ROOT_DIR"
   done
   rm -rf "$DATA_TMPEXTRACT"
fi

APPLICATION_CONTEXT_FILE="$INSTALL_PATH/servers/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/standard-technologies-applicationContext.xml"
if [ -f "$APPLICATION_CONTEXT_FILE" ]; then
   tmpFile="$BASE/xxx.xml"
   awk '/import/{gsub(/!*--/,"")}; 1' "$APPLICATION_CONTEXT_FILE" > "$tmpFile"
   mv "$tmpFile" "$APPLICATION_CONTEXT_FILE"
   for technology in $DISABLED_TECHNOLOGIES; do
     awk -v technology=${technology/,/} 'index($0, technology){gsub(/import/,"!--import") gsub(/>/,"-->")}; 1' "$APPLICATION_CONTEXT_FILE" > "$tmpFile"
     mv "$tmpFile" "$APPLICATION_CONTEXT_FILE"
   done
fi