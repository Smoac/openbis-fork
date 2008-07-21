#! /bin/bash

gcc -shared -O3 unixlink.c -I/usr/java/jdk1.5.0_16/include -I/usr/java/jdk1.5.0_16/include/linux -o jlink.so
