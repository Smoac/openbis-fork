#!/bin/bash
#
# 1) Mark all scripts as executable 
# 2) Clean up all temporary folders after installation 
#

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

INSTALL_TMPEXTRACT=$1
DATA_TMPEXTRACT=$2
DSS_ROOT_DIR=$3

source $BASE/chmodx-all-scripts.sh


if [ -d "$INSTALL_TMPEXTRACT" ]; then
  rm -rf $INSTALL_TMPEXTRACT
fi

if [ -d "$DATA_TMPEXTRACT" ]; then
   cp -Rf "$DATA_TMPEXTRACT/data/" "$DSS_ROOT_DIR"
   rm -rf "$DATA_TMPEXTRACT"
fi