#! /bin/bash

gcc -bundle -O3 -D_POSIX_C_SOURCE unix.c -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o unix.so
