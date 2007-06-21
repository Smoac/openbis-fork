create table database_version_logs
 (db_version           varchar(4) not null
 ,module_name          varchar(250)
 ,run_status           varchar(10)
 ,run_status_timestamp timestamp
 ,module_code          bytea
 ,run_exception        bytea
 );