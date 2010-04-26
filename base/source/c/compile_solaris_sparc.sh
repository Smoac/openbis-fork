#! /bin/bash

cc -G -KPIC -D_FILE_OFFSET_BITS=64 -D_LARGEFILE64_SOURCE -D_LARGEFILE_SOURCE -I/usr/java/include -I/usr/java/include/solaris unix.c -o libunix.so

# MACHINE_BYTE_ORDER=2 corresponds to 'big endian'
cc -G -KPIC -DMACHINE_BYTE_ORDER=2 copy*.c  -I/usr/java/include -I/usr/java/include/solaris -o libnativedata.so
