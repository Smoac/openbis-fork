#!/bin/bash

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

java -Djava.util.logging.config.file=$BASE/jul.config -jar $BASE/openBIS-installer.jar