#! /bin/bash

cc -G -KPIC -fast -D_FILE_OFFSET_BITS=64 -D_LARGEFILE64_SOURCE -D_LARGEFILE_SOURCE -I/usr/java/include -I/usr/java/include/solaris unix.c -o libunix.so

# MACHINE_BYTE_ORDER=1 corresponds to 'little endian'
cc -G -KPIC -fast -DMACHINE_BYTE_ORDER=1 copy*.c  -I/usr/java/include -I/usr/java/include/solaris -o libnativedata.so
