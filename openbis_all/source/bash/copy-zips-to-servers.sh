#!/bin/bash
# Copies all zip files from the current directory to the home directory of the specified servers

#addresses="cisd-openbis.ethz.ch 
addresses="agronomics.ethz.ch 
        openbis-liverx.ethz.ch
        imsb-us-openbis.ethz.ch 
        basysbio.ethz.ch 
        openbis-dsu.ethz.ch
	openbis-scu.ethz.ch"
#      openbis-phosphonetx.ethz.ch"

for address in $addresses; do
  echo Updating $address
  scp *.zip $address:.
done
