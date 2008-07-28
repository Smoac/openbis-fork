#! /bin/bash

gcc -shared -O3 unixlink.c -I/usr/java/jdk5/include -I/usr/java/jdk5/include/linux -o jlink.so
