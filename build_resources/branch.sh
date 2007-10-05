#! /bin/sh

if [ $# -ne 2 ]; then
    echo "Usage is $0 <project name> <branch>"
    exit 1
fi

name=$1
branch=$2

dir=`dirname $0`
ant -f $dir/ant/build.xml -Dname=$name -Dbranch=$branch branch
