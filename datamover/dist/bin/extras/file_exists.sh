#!/bin/bash
# author: Manuel Kohler, 2015
# Script which simplifies the Handshake (Data completion check) for the SIS datamover
# Expects a path as first parameter

PATH=$1
WHICH=/usr/bin/which
RM=/bin/rm
DIRNAME=`${WHICH} dirname`
BASENAME=`${WHICH} basename`

# check if absolute or relative path, if relative then get the absolute path
if [[ ${PATH} = /* ]]; then
	ABSOLUTE_PATH=${PATH}
	echo "Got absolue path: ${ABSOLUTE_PATH}"
else
	echo "Got relative path: ${PATH}"
	test -e "${PATH}" && ABSPATH=`cd \`${DIRNAME} "${PATH}"\`; pwd`"/"`${BASENAME} "${PATH}"`
    test -n "${ABSPATH}" && ABSOLUTE_PATH=${ABSPATH}
fi

BASENAME="${ABSOLUTE_PATH##*/}"
PATH="${ABSOLUTE_PATH%/*}"

MARKER_STRING=.MARKER_is_finished_
FILE_TO_TEST=${PATH}/${MARKER_STRING}${BASENAME}

if [[ -f ${FILE_TO_TEST} ]]; then
	echo "MARKER FILE ${FILE_TO_TEST} FOUND"
	# Need to remove the marker file
	${RM} ${FILE_TO_TEST}
	exit 0
else
	echo "MARKER FILE ${FILE_TO_TEST} NOT FOUND"
	exit 1
fi