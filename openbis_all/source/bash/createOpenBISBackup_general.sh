#! /bin/bash

# Creates a snapshot of the openbis database
# 2009, John Ryan, Manuel Kohler D-BSSE

export BACKUP_DIR=~/db_backups
export DATE=`/bin/date +%Y-%m-%d_%H%M`
export DAYS_TO_RETAIN=3
export DATABASES=$1

export MAIL_LIST="manuel.kohler@bsse.ethz.ch"
export BOX=`uname -n`
export PLATFORM=`uname -s`

if [ -z "${DATABASES}" ];
then
   export DATABASES=openbis_productive
fi

if [ $PLATFORM = "SunOS" ];
then
  export POSTGRES_BIN=/usr/postgres/8.4-community/bin/
  export MAILX="/usr/bin/mailx"
else 
  export POSTGRES_BIN=/usr/bin
  export MAILX="/bin/mail"
fi

umask 077
echo "* Creating snapshot of productive databases at $DATE"
for DATABASE in $DATABASES; do
    $POSTGRES_BIN/pg_dump -Uopenbis -Fc $DATABASE > $BACKUP_DIR/${DATE}_$DATABASE-db.dmp
    if [ $? -ne 0 ]; then
        echo -e "Postgres DB backup broken ... :-(" | $MAILX -s "Backup from $BOX for $DATABASE is BROKEN !" $MAIL_LIST
    else 
        echo -e "Postgres DB backup ran OK on $BOX for $DATABASE :-)" | $MAILX -s "Backup from $BOX for $DATABASE ran OK" $MAIL_LIST
    fi
done
/usr/bin/find $BACKUP_DIR -type f -mtime +$DAYS_TO_RETAIN -exec rm {} \;
echo " ** Finished ** "
