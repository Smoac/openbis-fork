#! /bin/bash

cc -G -KPIC -fast -m64 -I/usr/java/include -I/usr/java/include/solaris unix.c -o libunix.so
