OWNER=buczekp
psql -U postgres -c "drop database openbis_plasmids;"
psql -U postgres -c "create database openbis_plasmids with owner $OWNER encoding = 'UNICODE';"
psql -U $OWNER -d openbis_plasmids -f plasmids-YeastLab.sql 
