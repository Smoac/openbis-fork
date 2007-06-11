#! /bin/sh

if [ $# -eq 0 ]; then
    echo "Usage is $0 <project name> [<version>]"
    exit 1
fi

name=$1
version=trunk
if [ $# -gt 1 ]; then
    version=$2
fi

dir=`dirname $0`
cdir=`pwd`
build_dir=$cdir/tmp

rm -rf $build_dir
mkdir $build_dir
ant -lib $dir/lib/ecj.jar -f $dir/ant/build.xml \
    -Dname=$name \
    -Dversion=$version \
    -Ddir=$build_dir \
    -Dresult.dir=$cdir \
    build
