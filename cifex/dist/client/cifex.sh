#! /bin/sh

java -Xmx256m -Djavax.net.ssl.trustStore=etc/cifex-client.keystore -jar lib/cifex.jar "$@"
