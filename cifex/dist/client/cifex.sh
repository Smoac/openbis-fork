#! /bin/sh

CIFEX_ROOT=`dirname $0`

java -Xmx256m -Djavax.net.ssl.trustStore=etc/keystore -Dcifex.root=$CIFEX_ROOT -jar lib/cifex.jar "$@"
