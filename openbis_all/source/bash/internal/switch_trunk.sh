#! /bin/bash
# Internal CISD developers script for switching all projects back to a trunk from a spring branch. 
# 
# Copy it to ~/bin and then navigate to your Sprint branch working copy and invoke:
# 
# $ switch_trunk.sh 
#
# This will switch the working copy to trunk branch.
 
for d in *; do
  if [ -d "$d" -a "$d" != "test-output" ]; then
    cd $d
    svn switch svn+ssh://svncisd.ethz.ch/repos/cisd/$d/trunk
    cd ..
  fi
done