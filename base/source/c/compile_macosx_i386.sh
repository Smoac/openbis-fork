#! /bin/bash

gcc -m32 -bundle -O3 unix.c -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o libunix.jnilib

# MACHINE_BYTE_ORDER=1 corresponds to 'little endian'
gcc -m32 -bundle -O3 -DMACHINE_BYTE_ORDER=1 copy*.c -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o libnativedata.jnilib
