#!/bin/bash

#Detect Java version

if type -p java; then
    _java=java
else
    echo "Java not available"
	exit -1
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}'| cut -c1-3)
    if [[ "$version" > "1.7" ]]; then
        echo Java version $version found.
    else         
        echo Java version $version found is under the required 1.8.
        exit -1
    fi
fi

#Continue installation

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

java -Djava.util.logging.config.file=$BASE/jul.config -jar $BASE/openBIS-installer.jar