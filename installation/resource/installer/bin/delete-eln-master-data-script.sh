#!/bin/bash
BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

echo "Removing ELN master data script"
rm $BASE/../servers/core-plugins/eln-lims/1/as/initialize-master-data.py
