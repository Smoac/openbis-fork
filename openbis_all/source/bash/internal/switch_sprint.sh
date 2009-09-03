#! /bin/bash
# Internal CISD developers script for switching all projects to a sprint branch with given number. 
# 
# Copy it to ~/bin and then navigate to your Sprint branch working copy and invoke with a number:
# 
# $ switch_sprint.sh <SPRINT-NUMBER>
#
# This will switch the working copy to branch S<SPRINT-NUMBER>.x. 

SPRINT="$1"
if [ -z "$SPRINT" ]; then
  echo "Syntax: switch_sprint.sh <SPRINT-NUMBER>"
  exit 1
fi

for d in *; do 
  if [ -d "$d" -a "$d" != "test-output" ]; then
    cd $d 
    svn switch svn+ssh://svncisd.ethz.ch/repos/cisd/openbis_all/branches/sprint/S${SPRINT}.x/$d
    cd .. 
  fi
done
