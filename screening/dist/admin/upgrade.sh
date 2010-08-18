# Updates openbis for screening. 
# Assumes that openbis is installed in the parent directory of the directory where this script is located. 

OPENBIS_DB=openbis_screening_lmc
IMAGING_DB=imaging_productive

BASE=`dirname "$0"`
if [ ${BASE#/} == ${BASE} ]; then
    BASE="`pwd`/${BASE}"
fi

ROOT_DIR=$BASE/..
BACKUP_DIR=$ROOT_DIR/old

unalias cp
$BASE/down.sh

NOW=`date +%y%m%d-%H%m`

CONFIG=$BACKUP_DIR/config-backup-$NOW
$BASE/backup-config.sh $CONFIG

OLD_BIS=$BACKUP_DIR/openBIS-server-$NOW
mv $ROOT_DIR/openBIS-server $OLD_BIS 
mv $ROOT_DIR/datastore_server $BACKUP_DIR/datastore_server-$NOW

# pg_restore -d db-name db-file.dmp 
pg_dump -U openbis -Fc $OPENBIS_DB > $BACKUP_DIR/$OPENBIS_DB-${NOW}.dmp
pg_dump -U openbis -Fc $IMAGING_DB > $BACKUP_DIR/$IMAGING_DB-${NOW}.dmp

echo Installing openBIS Datastore Server
unzip $ROOT_DIR/datastore*.zip
cp $CONFIG/.keystore $ROOT_DIR/datastore_server/etc/openBIS.keystore

echo Installing openBIS Application Server
unzip $ROOT_DIR/openBIS-*.zip -d $ROOT_DIR
cp $CONFIG/.keystore $ROOT_DIR/openBIS-server/openBIS.keystore
./$ROOT_DIR/openBIS-server/install.sh $ROOT_DIR/openBIS-server

$BASE/restore-config-from-backup.sh $CONFIG
cp -r $OLD_BIS/jetty/indices/ $ROOT_DIR/openBIS-server/jetty/

mv $ROOT_DIR/*.zip $BACKUP_DIR/
$BASE/bisup.sh
sleep 20
$BASE/dssup.sh