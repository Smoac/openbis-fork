#! /bin/bash

gcc -shared -O3 unix.c -I/usr/java/jdk5/include -I/usr/java/jdk5/include/linux -o unix.so
