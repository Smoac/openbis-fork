#! /bin/bash

gcc -shared -O3 -fPIC unix.c -I/usr/java/jdk1.6.0/include -I/usr/java/jdk1.6.0/include/linux -o libunix.so

# MACHINE_BYTE_ORDER=1 corresponds to 'little endian'
gcc -shared -O3 -fPIC -DMACHINE_BYTE_ORDER=1 copy*.c  -I/usr/java/jdk1.6.0/include -I/usr/java/jdk1.6.0/include/linux -o libnativedata.so
