#!/bin/bash
# Starts up DSS

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi
$BASE/../datastore_server/datastore_server.sh start
