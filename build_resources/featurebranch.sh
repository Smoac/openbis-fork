#! /bin/sh

if [ $# -lt 2 -o $# -gt 3 ]; then
    echo "Usage is $0 <project name> <branch> [<revision>]"
    exit 1
fi

name=$1
branch=$2
if [ "$3" != "" ]; then
    revision=$3
else
    revision=HEAD
fi

dir=`dirname $0`
ant -f $dir/ant/build.xml -Dname=$name -Dbranch=$branch -Drevision=$revision feature-branch
