#! /bin/sh

if [ $# -ne 2 ]; then
    echo "Usage is $0 <project name> <tag>"
    exit 1
fi

name=$1
tag=$2

dir=`dirname $0`
ant -f $dir/ant/build.xml -Dname=$name -Dtag=$tag branch-and-tag
