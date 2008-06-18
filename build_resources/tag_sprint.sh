#! /bin/sh

if [ $# -ne 2 ]; then
    echo "Usage is $0 <project name> <sprint_tag>"
    echo "<sprint_tag> needs to start with 'S'"
    exit 1
fi

name=$1
tag=$2

dir=`dirname $0`
ant -f $dir/ant/build.xml -Dname=$name -Dtag=$tag tag-sprint
