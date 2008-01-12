#! /bin/sh

if [ $# -eq 0 ]; then
    echo "Usage is $0 [--private] [--revision <revision>] <project name> [<version>]"
    exit 1
fi

protocol=http
if [ "$1" = "--private" ]; then
	protocol=svn+ssh
	shift
fi
repository=$protocol://source.systemsx.ch/repos
if [ "$1" = "--revision" ]; then
	shift
	revision="$1"
	shift
else
	revision="HEAD"
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
		-DrepositoryRoot=$repository \
    -Dname=$name \
    -drevision=$revision \
    -Dversion=$version \
    -Ddir=$build_dir \
    -Dresult.dir=$cdir \
    build
