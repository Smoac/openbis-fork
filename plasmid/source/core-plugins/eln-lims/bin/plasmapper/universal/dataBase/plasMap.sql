
/*********************************************

DDL file  for PlasMapper

*********************************************/

/*
command to use dumped file to initialize the database
mysql -u xdong -p dblab < LIMS.sql

command to dump database into file	
mysqldump -u xdong -p dblab > xiaoli.txt

command to dump a set of rows from a table into to a file xiaoli_temp.txt

mysqldump -u xdong -p labm tblPersonnelSchedule "--where=user_ID='darndt'" > xiaoli_temp.txt

*/	



create database plasMap;

/*
	Create vector sequence Table
*/

drop table IF EXISTS vector;

CREATE TABLE vector(
	id varchar(40) NOT NULL,
	name varchar(200) not null,
	vendor varchar(40),
	sequence mediumblob,
	Primary Key(id)
)type=innodb;

