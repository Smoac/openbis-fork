#! /bin/bash
# 
# Fetches artifacts from continuous integration server for a specified project. Works only for Hudson/Jenkins.
# 
# Usage: fetch-ci-artifacts.sh  [-d <output folder>] [-p <regular expression for filtering artifact names>] <project>
#
# If <output folder> is specified the files are stored there. If it does not exist it will be created.
# By default the current directory will contain the artifacts.
# 
# If the p option is specified only artifact file names matching the regular expression will 
# be loaded from Hudson/Jenkins. 
#
set -o nounset
set -o errexit

CI_HOST=bs-ci01.ethz.ch:8090

if [ $# -lt 1 ]; then
    echo "Usage: fetch-ci-artifacts.sh [-d <output folder>] [-p <regular expression for filtering artifact names>] <project>"
    exit 1
fi

##################################################
#
# Gather parameters
#
PROJECT=${@: -1}
OUTPUT_FOLDER=.
PATTERN=".*"
while [ $# -ge 2 ]; do
    if [ $1 == "-d" ]; then
        shift
        if [ $# -lt 2 ]; then
            echo "Missing output folder for option -d."
            exit 1
        fi
        OUTPUT_FOLDER="$1"
        shift
    elif [ $1 == "-p" ]; then
        shift
        if [ $# -lt 2 ]; then
            echo "Missing pattern for option -p."
            exit 1
        fi
        PATTERN="$1"
        shift
    fi
done

PROJECT_BASE_URL=http://$CI_HOST/job/$PROJECT

##################################################
#
# Load list of available artifacts on Hudson/Jenkins 
#
XML=`curl -s "$PROJECT_BASE_URL/lastSuccessfulBuild/api/xml?xpath=//relativePath&wrapper=bag"`
if [ ${XML:0:4} != "<bag" ]; then
    echo "Couldn't get artifact information for project $PROJECT. Probably the project doesn't exist."
    exit 1
fi
if [ $XML == "<bag/>" ]; then
    echo "No artifacts for project $PROJECT"
    exit
fi
XML=${XML#<bag>}
XML=${XML%*</bag>}

##################################################
#
# Download artifacts
#
mkdir -p "$OUTPUT_FOLDER"
for p in `echo ${XML}|awk '{gsub(/<\/relativePath>/,"\n")}; 1'|awk '{gsub(/<relativePath>/,"")}; 1'|\
                      egrep "$PATTERN"|sort`; do
    f=${p##*/}
    download_url=$PROJECT_BASE_URL/lastSuccessfulBuild/artifact/$p
    echo "download $download_url"
    wget -q -O "$OUTPUT_FOLDER/$f" $download_url
done
