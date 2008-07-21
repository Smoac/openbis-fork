#! /bin/bash

gcc -bundle -O3 unixlink.c -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o jlink.so
