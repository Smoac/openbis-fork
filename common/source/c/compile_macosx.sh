#! /bin/bash

gcc -bundle unixlink.c -I/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o "i386-Mac OS X_jlink.so"
