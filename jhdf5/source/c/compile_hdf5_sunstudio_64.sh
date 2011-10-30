#! /bin/bash

PATH=/opt/SUNWspro/bin:/usr/local/bin:/opt/csw/bin:/usr/sbin:/usr/bin:/usr/openwin/bin:/usr/ccs/bin:/usr/ucb
export PATH

source version.sh

tar xf hdf5-$VERSION.tar

cd hdf5-$VERSION

CFLAGS='-fast -m64 -KPIC' ./configure --prefix=/opt/hdf5-$VERSION-64 --enable-shared --enable-debug=none --enable-production

make > make.log 2>&1

make test > test.log 2>&1
