#! /bin/bash

gcc -shared -O3 -fPIC unixlink.c -I/usr/java/jdk1.5.0_16/include -I/usr/java/jdk1.5.0_16/include/linux -o jlink.so
