#! /bin/bash

gcc -shared -O3 -fPIC unix.c -I/usr/java/jdk5/include -I/usr/java/jdk5/include/linux -o unix.so
