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

java -Xmx256m -Djavax.net.ssl.trustStore=${CIFEX_ROOT}/etc/keystore -Dcifex.root=${CIFEX_ROOT} -jar ${CIFEX_ROOT}/lib/cifex.jar "$@"
