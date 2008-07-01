#! /bin/bash

gcc -m64 -shared -fPIC -static-libgcc -R/usr/sfw/lib/amd64 unixlink.c -I/usr/java/include/ -I/usr/java/include/solaris -o amd64-SunOS_jlink.so
