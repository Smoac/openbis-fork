#!/bin/bash
# Shuts down openBIS and DSS

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

$BASE/dssdown.sh
$BASE/bisdown.sh

#Start up beewm if it's installed
beewmInstallDir=`echo ${BASE}/../servers/beewm/bee-*`
if [[ -d ${beewmInstallDir} ]]; then
        ${beewmInstallDir}/bee.sh stop
fi