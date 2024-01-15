# Configuration

## Environment Variables

| Variable | Default value | Description |
| ------------- | --------------------- | ------------------- |
|`OPENBIS_ADMIN_PASS`|`123456789`|Admin password to openBIS instance. |
|`OPENBIS_DATA`|`/data/openbis`|Directory for openBIS persistent data. |
|`OPENBIS_DB_ADMIN_PASS`|`mysecretpassword`|Postgres superuser password [1]. |
|`OPENBIS_DB_ADMIN_USER`|`postgres`|Postgres superuser name [1]. |
|`OPENBIS_DB_APP_PASS`|`mysecretpassword`|Password for application user connecting to the database. |
|`OPENBIS_DB_APP_USER`|`openbis`|Username for application user connecting to the dabase. |
|`OPENBIS_DB_HOST`|`postgres15`|Name of container running Postgres database [1]. |
|`OPENBIS_ETC`|`/etc/openbis`|Directory for openBIS configuration files. |
|`OPENBIS_HOME`|`/home/openbis`|Directory for openBIS installation. |
|`OPENBIS_LOG`|`/var/log/openbis`|Directory for openBIS log files. |

[1] Refer to the [PostgreSQL official image](https://hub.docker.com/_/postgres) to extend deployment details.

## Core Plugins 

TBD
