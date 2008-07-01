#! /bin/bash

gcc -shared unixlink.c -I/usr/java/jdk1.5.0_13/include -I/usr/java/jdk1.5.0_13/include/linux -o ../../../libraries/filelink/i386-Linux/jlink.so
