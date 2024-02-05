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

openBIS offers the ability to pass in configuration files like, e.g., capabilities files. Those can be deployed in any directory mounted as a volume in the openBIS docker container. It needs to be ensured that the associated AS and DSS properties are pointing to the correct file paths.

```{note}
It is necessary to store any data files that needs to be preserved inside the `openbis-app-data` volume, or inside some other docker volume. Likewise, any configuration files should be stored within the `openbis-app-etc` volume. Failure to do so yields undesired behavior in the sense that changes made to the files within the container are being lost when the container goes down.
```

### Examples

#### Suppy a json file for storing personal access tokens

1. Enable the feature

Add the following line to the AS service.properties:
```
personal-access-tokens-enabled = true
```

2. Ensure the AS property `personal-access-tokens-file-path` is pointing to the correct path to where the json file is located

Assuming the DSS root-dir points to the default dir, `/data/openbis`, then the personal-access-tokens.json could be stored in this directory:
```
personal-access-tokens-file-path = /data/openbis/personal-access-tokens.json
```

3. Create a PAT and monitor the contents of the json file

Please read on here: [personal-access-tokens.html#typical-application-workflow](../../software-developer-documentation/apis/personal-access-tokens.md#typical-application-workflow)

#### Modify the AS capabilities file

For this, it is not needed to 

## Core Plugins 

It is possible to make adjustments to core-plugins shipped with the openBIS installer. To do so, just start up openBIS at least once. This will copy the contents of the core-plugins directory to a sub-directory `core-plugins` which is stored within the docker volume `openbis-app-etc`. Any customizations made here will persist restarts of the application, as well as upgrades of the openbis-docker image.

```{warning}
Be careful when making changes to code of core-plugins since they might break when new releases are published. Please consider reading the [Change Log published with each release](https://unlimited.ethz.ch/display/openbis/Production+Releases).

If the application fails to start after changes to the core-plugins have been made, you can always revert to the original state of the core-plugins by removing the `core-plugins` folder within the `openbis-app-etc` volume.
```

Besides adjustments to existing plugins, it is also possible to [create new plugins from scratch](../../software-developer-documentation/server-side-extensions/core-plugins.md).

### Examples

#### Customize the InstanceProfile.js

This file is part of the `eln-lims` core-plugin. It is located here:
`<openbis-app-etc>/core-plugins/eln-lims/1/as/webapps/eln-lims/html/etc/InstanceProfile.js`

1. Make any changes to this file

E.g., change `this.showSemanticAnnotations = false;` to `this.showSemanticAnnotations = true;`.

2. Restart the container

Most changes made to the configuration of the openBIS application require a restart in order to be applied. Assuming the container running the openBIS application is named `openbis-app`, this is achieved as follows:
```
docker restart openbis-app
```