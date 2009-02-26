#! /bin/sh

CIFEX_ROOT=`dirname $0`

java -Xmx256m -Djavax.net.ssl.trustStore=${CIFEX_ROOT}/etc/keystore -Dcifex.root=${CIFEX_ROOT} -jar ${CIFEX_ROOT}/lib/cifex.jar "$@"
