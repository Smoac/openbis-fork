#!/bin/bash
#
# Sets the executable flag for all *.sh files within the openBIS installation folder
#
set -o errexit

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

# set all scripts executable
find $BASE/.. -type f -name "*.sh" -exec chmod 744 {} \;
