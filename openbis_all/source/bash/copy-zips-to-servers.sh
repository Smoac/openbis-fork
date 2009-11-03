#!/bin/bash
# Copies all zip files from the current directory to the home directory of the specified servers

export SPRINT=cisd-bamus.ethz.ch
export DEMO=cisd-tongariro.ethz.ch
export YEASTX=imsb-us-openbis.ethz.ch
export PHOSPHONETX=openbis-phosphonetx.ethz.ch
export LIVERX=openbis-liverx.ethz.ch
export AGRONOMICS=bs-dsvr11.ethz.ch
export DSU=bs-dsvr28-openbis-dsu.ethz.ch
export SCU=bs-dsvr28-openbis-scu.ethz.ch
export BASYSBIO=bs-dsvr10.ethz.ch

# Currently there are three different types of server specific zips we distinguish
export ZIPS="openBIS-server-S*.zip  datastore_server-S*.zip" 
export ZIPS_PHOSPHONETX=*phosphonetx*.zip
export ZIPS_DSU="openBIS-server-S*.zip datastore_server-dsu*.zip"

for i in $ZIPS; do
	 scp $i $DEMO
	 scp $i $YEASTX
	 scp $i $LIVERX
	 scp $i $AGRONOMICS
	 scp $i $SCU
	 scp $i $BASYSBIO
done     

for j in $ZIPS_PHOSPHONETX; do
	scp $j $PHOSPHONETX
done

for k in $ZIPS_DSU; do
	scp $k $DSU
done