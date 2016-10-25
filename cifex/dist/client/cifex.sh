#! /bin/sh

# This script requires the readlink binary. If your system lacks this binary, $JHDFDIR needs to be hard-coded

SCRIPT="$0"
BINDIR="${SCRIPT%/*}"
LINK="`readlink $0`"
while [ -n "${LINK}" ]; do
  if [ "${LINK#/}" = "${LINK}" ]; then
    SCRIPT="${BINDIR}/${LINK}"
  else
    SCRIPT="${LINK}"
  fi
  LINK="`readlink ${SCRIPT}`"
done
CIFEX_ROOT="`dirname ${SCRIPT}`"
CIFEX_ETC="${CIFEX_ROOT}/etc"
CIFEX_LIB="${CIFEX_ROOT}/lib"

java -Xmx256m -Dcifex.config="${CIFEX_ETC}" -jar "${CIFEX_LIB}/cifex.jar" "$@"
