#! /bin/bash

cc -G -KPIC -fast -xarch=amd64 -I/usr/java/include -I/usr/java/include/solaris unixlink.c -o jlink.so
