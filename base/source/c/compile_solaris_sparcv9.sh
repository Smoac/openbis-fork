#! /bin/bash

cc -G -KPIC -fast -m64 -I/usr/java/include -I/usr/java/include/solaris unix.c -o libunix.so

# MACHINE_BYTE_ORDER=2 corresponds to 'big endian'
cc -G -KPIC -fast -m64 -DMACHINE_BYTE_ORDER=2 copy*.c  -I/usr/java/include -I/usr/java/include/solaris -o libnativedata.so
