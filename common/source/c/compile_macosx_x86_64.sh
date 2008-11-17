#! /bin/bash

gcc -m64 -dynamiclib -O3 -D_POSIX_C_SOURCE unixlink.c -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o jlink.so
