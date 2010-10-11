#!/bin/bash
# Copies all zip files from the current directory to the home directory of the specified servers

export SPRINT=cisd-bamus.ethz.ch
export DEMO=cisd-tongariro.ethz.ch
export YEASTX=obis.ethz.ch
export PHOSPHONETX=openbis-phosphonetx.ethz.ch
export AGRONOMICS=bs-agronomics01.ethz.ch
export DSU=bs-dsvr28-openbis-dsu.ethz.ch
export SCU=bs-dsvr28-openbis-scu.ethz.ch
export BASYSBIO=basysbio.ethz.ch
export BASYSBIO_TEST=bs-dsvr28-openbis-test.ethz.ch
export CINA=bs-openbis01.ethz.ch
export PLASMIDS=bs-openbis02.ethz.ch


# Different types of server specific zips we distinguish
export ZIPS="openBIS-server-S*.zip  datastore_server-S*.zip"
export ZIPS_YEASTX="openBIS-server-S*.zip datastore_server-S*.zip *yeastx*.zip"
export ZIPS_PHOSPHONETX="datastore_server-S*.zip *phosphonetx*.zip"
export ZIPS_DSU="openBIS-server-S*.zip datastore_server-S*.zip datastore_server_plugin-dsu*.zip openbis-tracking-client*.zip"
export ZIPS_BASYSBIO="datastore_server-S*.zip *basysbio*.zip openBIS-server-S*.zip"
export ZIPS_CINA="openBIS-server-S*.zip datastore_server-S*.zip datastore_server_plugin-cina-*.zip"
export ZIPS_PLASMIDS="datastore_server-S*.zip *plasmid* openBIS-server-S*.zip"

echo -e "\nCopying default openBIS/DSS to servers...\n"
for i in $ZIPS; do
         echo $DEMO; scp -p $i $DEMO:~openbis
         echo $YEASTX; scp -p $i $YEASTX:~openbis
         echo $LIVERX; scp -p $i $LIVERX:~openbis
         echo $AGRONOMICS; scp -p $i $AGRONOMICS:~openbis
         echo $SCU; scp -p $i $SCU:~openbis
 
done

echo -e "\nCopying to $YEASTX...\n"
for j in $ZIPS_YEASTX; do
        echo $YEASTX; scp -p $j $YEASTX:~openbis
done

echo -e "\nCopying to $PHOSPHONETX...\n"
for j in $ZIPS_PHOSPHONETX; do
        echo $PHOSPHONETX; scp -p $j $PHOSPHONETX:~openbis
done

echo -e "\nCopying to $DSU...\n"
for k in $ZIPS_DSU; do
        echo $DSU; scp -p $k sbsuser@$DSU:~openbis
done

echo -e "\n Copying dss to $BASYSBIO...\n"
for m in $ZIPS_BASYSBIO; do
                echo $BASYSBIO; scp -p $m $BASYSBIO:~openbis
                echo $BASYSBIO_TEST; scp -p $m $BASYSBIO_TEST:~openbis
done


echo -e "\n Copying to $PLASMIDS...\n"
for y in $ZIPS_PLASMIDS; do
                echo $PLASMIDS; scp -p $y $PLASMIDS:~openbis
done


