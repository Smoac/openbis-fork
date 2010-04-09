#! /bin/bash

source version.sh

cc -G -KPIC -fast -D_FILE_OFFSET_BITS=64 -D_LARGEFILE64_SOURCE -D_LARGEFILE_SOURCE *.c -I/opt/hdf5-${VERSION}-32/include -I/usr/java/include -I/usr/java/include/solaris /opt/hdf5-${VERSION}-32/lib/libhdf5.a -lz -o libjhdf5.so
