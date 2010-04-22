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
export BASYSBIO_TEST=bs-dsvr28-openbis-test.ethz.ch
export CINA=bs-openbis01.ethz.ch


# Currently there are three different types of server specific zips we distinguish
export ZIPS="openBIS-server-S*.zip  datastore_server-S*.zip"
export ZIPS_PHOSPHONETX="*phosphonetx*.zip"
export ZIPS_DSU="openBIS-server-S*.zip datastore_server-dsu*.zip openbis-tracking-client*.zip"
export ZIPS_BASYSBIO="*basysbio*.zip openBIS-server-S*.zip"
export ZIPS_CINA="openBIS-server-S*.zip datastore_server-cina-*.zip"

# Special plugin
export DATASTORE_PLUGIN="datastore_server_plugin*.zip"

echo -e "\nCopying default openBIS/DSS to servers...\n"
for i in $ZIPS; do
         echo $DEMO; scp -p $i $DEMO:~openbis
         echo $YEASTX; scp -p $i $YEASTX:~openbis
         echo $LIVERX; scp -p $i $LIVERX:~openbis
         echo $AGRONOMICS; scp -p $i $AGRONOMICS:~openbis
         echo $SCU; scp -p $i $SCU:~openbis
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

echo -e "\n Copying to $CINA...\n"
for z in $ZIPS_CINA; do
                echo $CINA; scp -p $z $CINA:~openbis
done

echo -e "\nCopying to default dss...\n"
for l in $DATASTORE_PLUGIN; do
        echo $AGRONOMICS; scp -p $l $AGRONOMICS:~openbis/config
        echo $YEASTX; scp -p $l $YEASTX:~openbis/config
        echo $BASYSBIO; scp -p $l $BASYSBIO:~openbis/config
        echo $BASYSBIO_TEST; scp -p $l $BASYSBIO_TEST:~openbis/config
done