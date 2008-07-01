#! /bin/bash

gcc -shared -fPIC -static-libgcc unixlink.c -I/usr/java/include/ -I/usr/java/include/solaris -o x86-SunOS_jlink.so
