# Basic configuration

## Environment Variables

| Variable | Default value | Description |
| -------- | ------------- | ----------- |
|`OPENBIS_ADMIN_PASS`|`123456789`|Administrator password to openBIS instance.|
|`OPENBIS_DATA`|`/data/openbis`|Directory for openBIS persistent data.|
|`OPENBIS_DB_ADMIN_PASS`|`mysecretpassword`|PostgreSQL superuser password. |
|`OPENBIS_DB_ADMIN_USER`|`postgres`|PostgreSQL superuser name.|
|`OPENBIS_DB_APP_PASS`|`mysecretpassword`|Password for application user connecting to the database.|
|`OPENBIS_DB_APP_USER`|`openbis`|Username for application user connecting to the database.|
|`OPENBIS_DB_HOST`|`openbis-db`|Name of container running PostgreSQL database.|
|`OPENBIS_ETC`|`/etc/openbis`|Directory for openBIS configuration files.|
|`OPENBIS_HOME`|`/home/openbis`|Directory for openBIS installation binaries.|
|`OPENBIS_LOG`|`/var/log/openbis`|Directory for openBIS log files.|
|`OPENBIS_FQDN`|`openbis.domain`|Full qualified domain name of openBIS service.|

## Configuration Files

openBIS offers the ability to pass in configuration files like, e.g., capabilities files. Those can be deployed in any directory mounted as a volume in the openBIS docker container. It needs to be ensured that the associated AS and DSS properties are pointing to the correct folder locations.


## Core Plugins 

It is possible to adjust individual files and entire sub-directories within the core plugins shipped with the openBIS docker image by appropriately mounting those as a volume. Likewise, it is possible to create new plugins from scratch and mount those while starting up the openbis-app container.

### Example


1. Customization of the file InstanceProfile.js, which is part of the core-plugin `eln-lims`:

1.1. Create a file named `eln-lims-InstanceProfile.js` within the working directory from which you're running `docker-compose up` or `docker run`.

1.2.a) Running openBIS container with `docker run`

Append the following line to your `docker run` command invocation:
```
-v ./eln-lims-InstanceProfile.js:/data/core-plugins/eln-lims/1/as/webapps/eln-lims/html/etc/InstanceProfile.js
```

1.2.b) Running openBIS container via `docker compose`

Include the following line in your docker-compose.yml within the list of volumes attached to the `openbis-app` service:
```
- ./eln-lims-InstanceProfile.js:/data/core-plugins/eln-lims/1/as/webapps/eln-lims/html/etc/InstanceProfile.js
```
