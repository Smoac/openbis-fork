#! /bin/bash

cc -G -KPIC -fast -I/usr/java/include -I/usr/java/include/solaris unixlink.c -o jlink.so
