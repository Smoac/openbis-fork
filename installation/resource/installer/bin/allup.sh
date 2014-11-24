#!/bin/bash
# Starts up openBIS and DSS

if [ -n "$(readlink $0)" ]; then
   # handle symbolic links
   scriptName=$(readlink $0)
   if [[ "$scriptName" != /* ]]; then
      scriptName=$(dirname $0)/$scriptName
   fi
else
    scriptName=$0
fi

BASE=`dirname "$scriptName"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

$BASE/bisup.sh || exit 1;
$BASE/dssup.sh || exit 2;

#Start up beewm if it's installed
beewmInstallDir=`echo ${BASE}/../servers/beewm/bee-*`
if [[ -d ${beewmInstallDir} ]]; then
        ${beewmInstallDir}/bee.sh start || exit 3;
fi