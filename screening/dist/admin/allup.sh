#!/bin/bash
# Starts up openBIS and DSS

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

$BASE/bisup.sh || exit 1;
$BASE/dssup.sh || exit 2;