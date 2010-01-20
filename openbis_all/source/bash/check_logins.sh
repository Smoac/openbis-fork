#!/bin/bash

export DATE=`/bin/date +%Y-%m-%d_%H%M`
export MAIL_LIST="manuel.kohler@bsse.ethz.ch claus.hultschig@bsse.ethz.ch"
export DIR=/localhome/openbis/logs/checks
export MAILX=/bin/mailx

/localhome/openbis/logs/logins.sh > $DIR/logins_$DATE.txt

export SECOND_LATEST=`ls -t1 $DIR | tail -n 1`
export LATEST=`ls -t1 $DIR | head -n 1`

#echo "diff -q $DIR/$SECOND_LATEST $DIR/$LATEST"
diff -q $DIR/$SECOND_LATEST $DIR/$LATEST

if [ $? -ne 0 ]; then
        export LAST_6_LINES=`tail -n 6 $DIR/$LATEST`
        echo -e "$LAST_6_LINES" | $MAILX -s "Login to Basysbio detected!" $MAIL_LIST
fi   

/usr/bin/find $DIR -type f -mmin +1 -exec rm {} \;