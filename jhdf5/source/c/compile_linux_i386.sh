gcc -shared -O3 *.c -I/opt/hdf5-1.8.4-i386/include -I/usr/java/jdk1.5.0_16/include -I/usr/java/jdk1.5.0_16/include/linux /opt/hdf5-1.8.4-i386/lib/libhdf5.a -o libjhdf5.so -lz
