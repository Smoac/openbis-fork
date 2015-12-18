#!/bin/bash

psql -dopenbis_dev -A -t -c \
"SELECT content FROM events WHERE identifiers LIKE '$1, %' OR identifiers LIKE '%, $1' OR identifiers LIKE '%, $1,%' OR identifiers = '$1'" | 
python formatter.py $1