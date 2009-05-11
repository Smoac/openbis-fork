# this small script updates all scripts available in this directory from svn

SVN=http://svncisd.ethz.ch/repos/cisd/openbis_all/trunk/source/bash
for f in *.sh; do
  if [ "$f" != $0 ]; then  
  wget $SVN/$f
done