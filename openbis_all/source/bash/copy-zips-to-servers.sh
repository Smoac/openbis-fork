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
export ZIPS_PHOSPHONETX="*phosphonetx*.zip"
export ZIPS_DSU="openBIS-server-S*.zip datastore_server-dsu*.zip"

# Special plugin
export DATASTORE_PLUGIN="datastore_server-plugins.jar"

for i in $ZIPS; do
	 echo $DEMO; scp -p $i $DEMO
	 echo $YEASTX; scp -p $i $YEASTX
	 echo $LIVERX; scp -p $i $LIVERX
	 echo $AGRONOMICS; scp -p $i $AGRONOMICS
	 echo $SCU; scp -p $i $SCU
	 echo $BASYSBIO; scp -p $i $BASYSBIO
done     

for j in $ZIPS_PHOSPHONETX; do
	echo $PHOSPHONETX; scp -p $j $PHOSPHONETX
done

for k in $ZIPS_DSU; do
	echo $DSU; scp -p $k $DSU
done

for l in $DATASTORE_PLUGIN; do
	echo $BASYSBIO; scp -p $k $BASYSBIO
	echo $AGRONOMICS; scp -p $k $AGRONOMICS
	echo $YEASTX; scp -p $k $YEASTX
done