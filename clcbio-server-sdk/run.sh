#!/bin/bash

OS=`uname`
ARCH=`uname -p`

if [ $OS = "Darwin" ]; then
   OSNAME="macosx"
else
   OSNAME="linux"   
fi

if [ $ARCH = "x86_64" ]; then
   ARCHNAME="x86_64"
else
   ARCHNAME="x86"
fi
NATIVEDIR=${OSNAME}_${ARCHNAME}

java -Xmx512m -XX:MaxPermSize=192m -Djava.awt.headless=true -Djava.library.path=./webapps/CLCServer/WEB-INF/native:./webapps/CLCServer/WEB-INF/native/$NATIVEDIR -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file=./conf/logging.properties -Djava.endorsed.dirs=./endorsed -Dcatalina.base=./ -Dcatalina.home=./ -cp bin/bootstrap.jar:./conf org.apache.catalina.startup.Bootstrap
