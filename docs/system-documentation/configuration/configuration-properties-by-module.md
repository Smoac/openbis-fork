# CORE MODULES 

## AS  MODULES


| Key                                                                                    | Example Value                         | Short Explanation                                                                                                                                       | More |
|----------------------------------------------------------------------------------------|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|------|
| database-instance                                                                      | DEFAULT                               | The database instance local unique identifier.                                                                                                          |      |
| database.url-host-part                                                                 | localhost                             | The host and optionally port.                                                                                                                           |      |
| database.kind                                                                          | prod                                  |                                                                                                                                                         |      |
| database.owner                                                                         |                                       | User who owns the database. Default: Operating system user running the server.                                                                          |      |
| database.owner-password                                                                |                                       |                                                                                                                                                         |      |
| database.admin-user                                                                    |                                       | Superuser of the database.                                                                                                                              |      |
| database.admin-password                                                                |                                       |                                                                                                                                                         |      |
| database.max-active-connections                                                        | 20                                    | Max. number of active database connections.                                                                                                             |      |
| database.max-idle-connections                                                          | 20                                    | Max. number of idle database connections to keep open.                                                                                                  |      |
| database.active-connections-log-interval                                               | 3600                                  | Log interval (in seconds) between two regular log entries of the number of active database                                                              |      |
| download-url                                                                           | https://localhost:8443                | The base URL for Web client access.                                                                                                                     |      |
| export.data-limit                                                                      | 10737418240                           | Export data limit in bytes                                                                                                                              |      |
| xls-import.version-data-file                                                           | ../../../xls-import-version-info.json | Path to the file which stores version information of master data imported from Excel sheets.                                                            |      |
| imaging-database.url-host-part                                                         |                                       | Imaging database for screening and microscopy                                                                                                           |      |
| imaging-database.kind                                                                  |                                       |                                                                                                                                                         |      |
| imaging-database.owner                                                                 |                                       |                                                                                                                                                         |      |
| imaging-database.password                                                              |                                       |                                                                                                                                                         |      |
| authentication-service                                                                 | file-authentication-service           | Authentication configuration                                                                                                                            |      |
| allow-missing-user-creation                                                            | false                                 | When a new person is created in the database the authentication service is asked by default whether this person is known by the authentication service. |      |
| crowd.service.host                                                                     |                                       | Crowd configuration                                                                                                                                     |      |
| crowd.service.port                                                                     |                                       |                                                                                                                                                         |      |
| crowd.application.name                                                                 |                                       |                                                                                                                                                         |      |
| crowd.application.password                                                             |                                       |                                                                                                                                                         |      |
| ldap.server.url                                                                        | <LDAP URL1> <LDAP URL2>               | The space-separated URLs of the LDAP servers                                                                                                            |      |
| ldap.security.principal.distinguished.name                                             |                                       | The distinguished name of the security principal                                                                                                        |      |
| ldap.security.principal.password                                                       |                                       |                                                                                                                                                         |      |
| ldap.security.protocol                                                                 |                                       |                                                                                                                                                         |      |
| ldap.security.authentication-method                                                    |                                       |                                                                                                                                                         |      |
| ldap.referral                                                                          |                                       |                                                                                                                                                         |      |
| ldap.searchBase                                                                        |                                       |                                                                                                                                                         |      |
| ldap.attributenames.user.id                                                            |                                       |                                                                                                                                                         |      |
| ldap.attributenames.email                                                              |                                       |                                                                                                                                                         |      |
| ldap.attributenames.first.name                                                         |                                       |                                                                                                                                                         |      |
| ldap.attributenames.last.name                                                          |                                       |                                                                                                                                                         |      |
| ldap.queryEmailForAliases                                                              |                                       |                                                                                                                                                         |      |
| ldap.queryTemplate                                                                     |                                       |                                                                                                                                                         |      |
| ldap.maxRetries                                                                        |                                       |                                                                                                                                                         |      |
| ldap.timeout                                                                           |                                       |                                                                                                                                                         |      |
| ldap.timeToWaitAfterFailure                                                            |                                       |                                                                                                                                                         |      |
| user-for-anonymous-login                                                               |                                       | Login of the existing user whose settings will be used for anonymous login                                                                              |      |
| authorization.project-level.enabled                                                    |                                       |                                                                                                                                                         |      |
| authorization.project-level.users                                                      |                                       |                                                                                                                                                         |      |
| project-samples-enabled                                                                |                                       |                                                                                                                                                         |      |
| web-client-configuration-file                                                          |                                       |                                                                                                                                                         |      |
| trusted-cross-origin-domains                                                           |                                       |                                                                                                                                                         |      |
| session-timeout                                                                        |                                       | The time after which an inactive session is expired by the service (in minutes).                                                                        |      |
| session-timeout-no-login                                                               |                                       |                                                                                                                                                         |      |
| max-number-of-sessions-per-user                                                        |                                       |                                                                                                                                                         |      |
| users-with-unrestricted-number-of-sessions                                             |                                       |                                                                                                                                                         |      |
| material-relax-code-constraints                                                        |                                       |                                                                                                                                                         |      |
| data-set-types-with-no-experiment-needed                                               |                                       |                                                                                                                                                         |      |
| create-continuous-sample-codes                                                         |                                       |                                                                                                                                                         |      |
| entity-history.enabled                                                                 |                                       |                                                                                                                                                         |      |
| onlinehelp.generic.root-url                                                            |                                       |                                                                                                                                                         |      |
| onlinehelp.generic.page-template                                                       |                                       |                                                                                                                                                         |      |
| openbis.support.email                                                                  |                                       |                                                                                                                                                         |      |
| memorymonitor-monitoring-interval                                                      |                                       |                                                                                                                                                         |      |
| memorymonitor-log-interval                                                             |                                       |                                                                                                                                                         |      |
| memorymonitor-high-watermark-percent                                                   |                                       |                                                                                                                                                         |      |
| query-databases                                                                        |                                       | Database Configurations for Query module                                                                                                                |      |
| openbisDB.label                                                                        |                                       |                                                                                                                                                         |      |
| openbisDB.data-space                                                                   |                                       |                                                                                                                                                         |      |
| openbisDB.creator-minimal-role                                                         |                                       |                                                                                                                                                         |      |
| openbisDB.database-driver                                                              |                                       |                                                                                                                                                         |      |
| openbisDB.database-url                                                                 |                                       |                                                                                                                                                         |      |
| openbisDB.database-username                                                            |                                       |                                                                                                                                                         |      |
| openbisDB.database-password                                                            |                                       |                                                                                                                                                         |      |
| maintenance-plugins                                                                    |                                       | Maintenance plugins configuration                                                                                                                       |      |
| authorization-component-factory                                                        | Internal - do not change              |                                                                                                                                                         |      |
| script-folder                                                                          |                                       | Internal - do not change                                                                                                                                |      |
| jython-version                                                                         |                                       | Internal - do not change                                                                                                                                |      |
| api.v3.operation-execution.store.path                                                  |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.name                                            |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.core-size                                       |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.max-size                                        |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.keep-alive-time                                 |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.progress.thread-name                                        |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.progress.interval                                           |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.default                                   |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.max                                       |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.summary.default                           |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.summary.max                               |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.details.default                           |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.details.max                               |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timeout-pending-task.name          |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timeout-pending-task.interval      |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timed-out-or-deleted-task.name     |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timed-out-or-deleted-task.interval |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.state-update.mark-failed-after-server-restart-task.name     |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.capacity                                              |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.class                                                 |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.directory                                             |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.clearance-task-name                                   |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.timeout                                               |                                       |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.timeout-check-interval                                |                                       |                                                                                                                                                         |      |
| api.v3.transaction.enabled                                                             |                                       |                                                                                                                                                         |      |
| api.v3.transaction.coordinator-key                                                     |                                       |                                                                                                                                                         |      |
| api.v3.transaction.interactive-session-key                                             |                                       |                                                                                                                                                         |      |
| api.v3.transaction.transaction-count-limit                                             |                                       |                                                                                                                                                         |      |
| api.v3.transaction.transaction-timeout                                                 |                                       |                                                                                                                                                         |      |
| api.v3.transaction.finish-transactions-interval                                        |                                       |                                                                                                                                                         |      |
| api.v3.transaction.transaction-log-folder-path                                         |                                       |                                                                                                                                                         |      |
| api.v3.transaction.participant.application-server.url                                  |                                       |                                                                                                                                                         |      |
| api.v3.transaction.participant.application-server.timeout                              |                                       |                                                                                                                                                         |      |
| api.v3.transaction.participant.afs-server.url                                          |                                       |                                                                                                                                                         |      |
| api.v3.transaction.participant.afs-server.timeout                                      |                                       |                                                                                                                                                         |      |
| server-public-information.afs-server.url                                               |                                       |                                                                                                                                                         |      |


## DSS MODULES


| Key                                                    | Example Value                         | Short Explanation                                                                                                          | More |
|--------------------------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------|------|
| data-store-server-code                                 | DSS1                                  | Unique code of this Data Store Server.                                                                                     |      |
| host-address                                           | https://localhost                     | host name of the machine on which the datastore server is running                                                          |      |
| root-dir                                               |                                       | parent directory of the store directory and all the dropboxes                                                              |      |
| storeroot-dir                                          | ${root-dir}/store                     | The root directory of the data store                                                                                       |      |
| incoming-root-dir                                      | ${root-dir}                           |                                                                                                                            |      |
| cache-workspace-folder                                 |                                       | Cache for data set files from other Data Store Servers                                                                     |      |
| session-workspace-root-dir                             |                                       |                                                                                                                            |      |
| commandqueue-dir                                       | ../../datastore_commandqueue          | The directory where the command queue file is located                                                                      |      |
| data-set-command-queue-mapping                         |                                       |                                                                                                                            |      |
| port                                                   | 8444                                  |                                                                                                                            |      |
| use-ssl                                                | true                                  |                                                                                                                            |      |
| session-timeout                                        | 720                                   | Session timeout in minutes                                                                                                 |      |
| keystore.path                                          |                                       | Path to the keystore                                                                                                       |      |
| keystore.password                                      |                                       | Password of the keystore                                                                                                   |      |
| keystore.key-password                                  |                                       | Key password of the keystore                                                                                               |      |
| check-interval                                         | 5                                     | The check interval (in seconds)                                                                                            |      |
| quiet-period                                           | 10                                    |                                                                                                                            |      |
| shutdown-timeout                                       | 180                                   | The time-out for clean up work in the shutdown sequence (in seconds).                                                      |      |
| minimum-time-to-keep-streams-in-sec                    | 20                                    | The minimum time (in seconds) of availability of the data stream since moment when user requested for the data stream url. |      |
| highwater-mark                                         | -1                                    | Data Set Registration Halt                                                                                                 |      |
| recovery-highwater-mark                                | -1                                    | Data Set Registration Halt                                                                                                 |      |
| notify-successful-registration                         | false                                 |                                                                                                                            |      |
| server-url                                             | ${host-address}:8443                  | The URL of the openBIS server                                                                                              |      |
| username                                               | etlserver                             | The username to use when contacting the openBIS server                                                                     |      |
| password                                               |                                       | The password for the etlserver user who contacts the openBIS server                                                        |      |
| download-url                                           | ${host-address}:${port}               | The base URL for Web client access to the data store server.                                                               |      |
| mail.smtp.host                                         |                                       | SMTP properties                                                                                                            |      |
| mail.from                                              |                                       |                                                                                                                            |      |
| mail.smtp.user                                         |                                       |                                                                                                                            |      |
| mail.smtp.password                                     |                                       |                                                                                                                            |      |
| data-sources                                           | path-info-db                          | Data sources                                                                                                               |      |
| path-info-db.version-holder-class                      |                                       |                                                                                                                            |      |
| path-info-db.databaseEngineCode                        |                                       |                                                                                                                            |      |
| path-info-db.basicDatabaseName                         |                                       |                                                                                                                            |      |
| path-info-db.urlHostPart                               |                                       |                                                                                                                            |      |
| path-info-db.databaseKind                              |                                       |                                                                                                                            |      |
| path-info-db.scriptFolder                              |                                       |                                                                                                                            |      |
| path-info-db.owner                                     |                                       | The owner of the database                                                                                                  |      | 
| path-info-db.password                                  |                                       | Owner password                                                                                                             |      |
| path-info-db.adminUser                                 |                                       | The administrator user of the database server.                                                                             |      |
| path-info-db.adminPassword                             |                                       | Administrator password                                                                                                     |      |
| maintenance-plugins                                    | post-registration, path-info-deletion | Comma separated names of maintenance plugins.                                                                              |      |
| post-registration.class                                |                                       | Maintenance task for post registration of all paths of a freshly registered data set                                       |      |
| post-registration.interval                             | 30                                    |                                                                                                                            |      |
| post-registration.cleanup-tasks-folder                 |                                       |                                                                                                                            |      |
| post-registration.last-seen-data-set-file              |                                       |                                                                                                                            |      |
| post-registration.post-registration-tasks              | pathinfo-feeding                      |                                                                                                                            |      |
| post-registration.pathinfo-feeding.class               |                                       |                                                                                                                            |      |
| post-registration.pathinfo-feeding.compute-checksum    | true                                  |                                                                                                                            |      |
| path-info-deletion.class                               |                                       | Maintenance task for deleting entries from pathinfo database                                                               |      |
| path-info-deletion.interval                            |                                       |                                                                                                                            |      |
| path-info-deletion.data-source                         |                                       |                                                                                                                            |      |
| path-info-deletion.data-set-table-name                 |                                       |                                                                                                                            |      |
| path-info-deletion.data-set-perm-id                    |                                       |                                                                                                                            |      |
| processing-plugins                                     | path-info-db-consistency-check        | Comma separated names of processing plugins.                                                                               |      |
| path-info-db-consistency-check.label                   |                                       | Processing task that checks the consistency between the data store and the meta information stored in the PathInfoDB.      |      |
| path-info-db-consistency-check.dataset-types           |                                       |                                                                                                                            |      |
| path-info-db-consistency-check.class                   |                                       |                                                                                                                            |      |
| inputs                                                 | default-dropbox                       |                                                                                                                            |      |
| dss-rpc.put-default                                    | default-dropbox                       |                                                                                                                            |      |
| default-dropbox.incoming-dir                           | ${incoming-root-dir}/incoming-default |                                                                                                                            |      |
| default-dropbox.incoming-data-completeness-condition   | auto-detection                        |                                                                                                                            |      |
| default-dropbox.top-level-data-set-handler             |                                       |                                                                                                                            |      |
| default-dropbox.program-class                          |                                       |                                                                                                                            |      |
| default-dropbox.storage-processor                      |                                       |                                                                                                                            |      |
| archiver.class                                         |                                       | Archiver class specification                                                                                               |      |
| imaging-database.url-host-part                         |                                       | Imaging database for screening and microscopy                                                                              |      |
| imaging-database.kind                                  |                                       | Imaging database for screening and microscopy                                                                              |      |
| imaging-database.owner                                 |                                       | Imaging database for screening and microscopy                                                                              |      |
| imaging-database.password                              |                                       | Imaging database for screening and microscopy                                                                              |      |
| imaging-database.adminUser                             |                                       | Imaging database for screening and microscopy                                                                              |      |
| imaging-database.adminPassword                         |                                       | Imaging database for screening and microscopy                                                                              |      |
| ftp.server.enable                                      | true                                  | When set to 'true' an internal ftp / sftp server will be started.                                                          |      |
| ftp.server.sftp-port                                   | 2222                                  | SFTP port                                                                                                                  |      |
| ftp.resolver-dev-mode                                  |                                       |                                                                                                                            |      |
| ftp.server.certificate                                 |                                       |                                                                                                                            |      |
| rsync-options                                          |                                       | Typical options to disable coping general, owner and group permissions                                                     |      |
| api.v3.fast-download.maximum-number-of-allowed-streams | 10                                    | The maximum number of allowed download streams.                                                                            |      |
| jython-version                                         |                                       |                                                                                                                            |      |
|                                                        |                                       |                                                                                                                            |      |







## PLUGIN MODULES
Plugin modules, with few exceptions, follow the naming schema in lower-case characters:
`<core-plugin>.<server-type>.<plugin-type>.<plugin-name>.<plugin-property-name>`

e.g

`eln-lims.as.miscellaneous.file-service.file-server.repository-path`

| Core Plugin | Server Type | Plugin Type   | Plugin Name  | Plugin Property Name        |
|-------------|-------------|---------------|--------------|-----------------------------|
| eln-lims    | as          | miscellaneous | file-service | file-server.repository-path |


### ELN

#### AS PROPERTIES

| Key                                                                        | Example Value | Short Explanation | More |
|----------------------------------------------------------------------------|---------------|-------------------|------|
| eln-lims.as.miscellaneous.file-service.file-server.maximum-file-size-in-MB |               |                   |      |
| eln-lims.as.miscellaneous.file-service.file-server.repository-path         |               |                   |      |
| eln-lims.as.services.as-eln-lims-api.script-path                           |               |                   |      |
| eln-lims.as.services.freeze-api.script-path                                |               |                   |      |
| eln-lims.as.services.publication-api.script-path                           |               |                   |      |
| eln-lims.as.webapps.eln-lims.webapp-folder                                 |               |                   |      |
| file-server.section_eln-lims.download-url-template                         |               |                   |      |
| file-server.section_eln-lims.error-message-template                        |               |                   |      |

#### DSS PROPERTIES

| Key                                                                                  | Example Value | Short Explanation | More |
|--------------------------------------------------------------------------------------|---------------|-------------------|------|
| eln-lims.dss.drop-boxes.eln-lims-dropbox.incoming-data-completeness-condition        |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.top-level-data-set-handler                  |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.script-path                                 |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.storage-processor                           |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.discard-files-patterns                      |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.illegal-files-patterns                      |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.incoming-data-completeness-condition |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.top-level-data-set-handler           |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.script-path                          |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.storage-processor                    |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.discard-files-patterns               |               |                   |      |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.illegal-files-patterns               |               |                   |      |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.resolver-class            |               |                   |      |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.code                      |               |                   |      |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.script-file               |               |                   |      |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.ftp.resolver-dev-mode     |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.interval                                      |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.dataset-types                                 |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.entity-sequence-properties                    |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.file-types                                    |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.last-seen-data-set-file                       |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.blast-databases-folder                        |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.blast-temp-folder                             |               |                   |      |
| eln-lims.dss.maintenance-tasks.blastdb.blast-tools-directory                         |               |                   |      |
| eln-lims.dss.reporting-plugins.archiving-api.sub-directory-name                      |               |                   |      |
| eln-lims.dss.reporting-plugins.archiving-api.label                                   |               |                   |      |
| eln-lims.dss.reporting-plugins.dropbox-monitor-api.label                             |               |                   |      |
| eln-lims.dss.reporting-plugins.dropbox-monitor-api.script-path                       |               |                   |      |
| eln-lims.dss.reporting-plugins.dropbox-monitor-api.share-id                          |               |                   |      |
| eln-lims.dss.reporting-plugins.eln-lims-api.label                                    |               |                   |      |
| eln-lims.dss.reporting-plugins.eln-lims-api.script-path                              |               |                   |      |
| eln-lims.dss.reporting-plugins.eln-lims-api.share-id                                 |               |                   |      |
| eln-lims.dss.reporting-plugins.exports-api.label                                     |               |                   |      |
| eln-lims.dss.reporting-plugins.exports-api.script-path                               |               |                   |      |
| eln-lims.dss.reporting-plugins.exports-api.limit-data-size-megabytes                 |               |                   |      |
| eln-lims.dss.reporting-plugins.exports-api.share-id                                  |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.label                              |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.script-path                        |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.max-delay-in-minutes               |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.password-reset-request-subject     |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.password-reset-request-body        |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.new-password-subject               |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.new-password-body                  |               |                   |      |
| eln-lims.dss.reporting-plugins.password-reset-api.share-id                           |               |                   |      |
| eln-lims.dss.reporting-plugins.rc-exports-api.label                                  |               |                   |      |
| eln-lims.dss.reporting-plugins.rc-exports-api.script-path                            |               |                   |      |
| eln-lims.dss.reporting-plugins.rc-exports-api.realm                                  |               |                   |      |
| eln-lims.dss.reporting-plugins.rc-exports-api.share-id                               |               |                   |      |
| eln-lims.dss.reporting-plugins.zenodo-exports-api.label                              |               |                   |      |
| eln-lims.dss.reporting-plugins.zenodo-exports-api.script-path                        |               |                   |      |
| eln-lims.dss.reporting-plugins.zenodo-exports-api.share-id                           |               |                   |      |
| zenodo-exports-api-limit-data-size-megabytes                                         |               |                   |      |
| zenodo-exports-api-zenodoUrl                                                         |               |                   |      |
| zenodo-exports-api-accessToken                                                       |               |                   |      |
| eln-lims.dss.search-domain-services.blastsearch.blast-tools-directory                |               |                   |      |
| eln-lims.dss.search-domain-services.blastsearch.blast-databases-folder               |               |                   |      |
| rc-exports-api-limit-data-size-megabytes                                             |               |                   |      |
| rc-exports-api-service-document-url                                                  |               |                   |      |
| rc-exports-api-user                                                                  |               |                   |      |
| rc-exports-api-password                                                              |               |                   |      |
| dataset-uploader.h5-folders                                                          |               |                   |      |
| dataset-uploader.h5ar-folders                                                        |               |                   |      |
| default-incoming-share-id                                                            |               |                   |      |
| default-incoming-share-minimum-free-space-in-gb                                      |               |                   |      |
| download-url                                                                         |               |                   |      |

### SCREENING

#### AS PROPERTIES


| Key                                                                                     | Example Value | Short Explanation | More |
|-----------------------------------------------------------------------------------------|---------------|-------------------|------|
| screening.as.miscellaneous.screening-application.screening-application-context-required |               |                   |      |

#### DSS PROPERTIES


| Key                                                                                   | Example Value | Short Explanation | More |
|---------------------------------------------------------------------------------------|---------------|-------------------|------|
| screening.dss.drop-boxes.hcs-dropbox.incoming-data-completeness-condition             |               |                   |      |
| screening.dss.drop-boxes.hcs-dropbox.top-level-data-set-handler                       |               |                   |      |
| screening.dss.drop-boxes.hcs-dropbox.script-path                                      |               |                   |      |
| screening.dss.drop-boxes.hcs-dropbox.storage-processor                                |               |                   |      |
| screening.dss.drop-boxes.hcs-dropbox.storage-processor.data-source                    |               |                   |      |
| screening.dss.drop-boxes.hcs-dropbox.storage-processor.define-channels-per-experiment |               |                   |      |
| screening.dss.drop-boxes.hcs-dropbox.storage-processor.notify-if-incomplete           |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.incoming-data-completeness-condition          |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.top-level-data-set-handler                    |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.script-path                                   |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.storage-processor                             |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.unzip                                         |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.delete_unzipped                               |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.do-not-create-original-dir                    |               |                   |      |
| screening.dss.drop-boxes.simple-dropbox.move-unregistered-datasets-to-error-dir       |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.interval                          |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.data-source                       |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.start                             |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.execute-only-once                 |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.retry-intervals-after-failure     |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.run-schedule                      |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.run-schedule-file                 |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.synchronization-table             |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.last-seen-event-id-column         |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.data-set-table-name               |               |                   |      |
| screening.dss.maintenance-tasks.imaging-db-clean-up.data-set-perm-id                  |               |                   |      |
| screening.dss.reporting-plugins.default-plate-image-analysis.sub-directory-name       |               |                   |      |
| screening.dss.servlet-services.screening-dss-api.path                                 |               |                   |      |
| screening.dss.servlet-services.screening-dss-api-json.path                            |               |                   |      |
| screening.dss.servlet-services.screening-images.path                                  |               |                   |      |


### ADMIN

#### AS PROPERTIES


| Key                                | Example Value | Short Explanation | More |
|------------------------------------|---------------|-------------------|------|
| admin.as.admin-service.class       |               |                   |      |
| admin.as.admin-service.script-path |               |                   |      |
| admin.as.xls-export.class          |               |                   |      |
| admin.as.xls-export.script-path    |               |                   |      |
| admin.as.xls-import.class          |               |                   |      |
| admin.as.xls-import.script-path    |               |                   |      |
| admin.as.webapps.webapp-folder     |               |                   |      |

#### DSS PROPERTIES

None.

### DATASET-UPLOADER

#### AS PROPERTIES


| Key                                                                  | Example Value | Short Explanation | More |
|----------------------------------------------------------------------|---------------|-------------------|------|
| dataset-uploader.as.webapps.dataset-uploader.webapp-folder           |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.openbisui-contexts      |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.sample-entity-types     |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.experiment-entity-types |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.label                   |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.sorting                 |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.data-set-entity-types   |               |                   |      |
| dataset-uploader.as.webapps.dataset-uploader.material-entity-types   |               |                   |      |

#### DSS PROPERTIES


| Key                                                                     | Example Value | Short Explanation | More |
|-------------------------------------------------------------------------|---------------|-------------------|------|
| dataset-uploader.dss.reporting-plugins.dataset-uploader-api.label       |               |                   |      |
| dataset-uploader.dss.reporting-plugins.dataset-uploader-api.script-path |               |                   |      |
| dataset-uploader.dss.reporting-plugins.dataset-uploader-api.share-id    |               |                   |      |
| dataset-uploader.h5-folders                                             |               |                   |      |
| dataset-uploader.h5ar-folders                                           |               |                   |      |

### DROPBOX-MONITOR

#### AS PROPERTIES


| Key                                                               | Example Value | Short Explanation | More |
|-------------------------------------------------------------------|---------------|-------------------|------|
| dropbox-monitor.as.webapps.dropBoxMonitor.label                   |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.webapp-folder           |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.openbisui-contexts      |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.sample-entity-types     |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.experiment-entity-types |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.sorting                 |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.data-set-entity-types   |               |                   |      |
| dropbox-monitor.as.webapps.dropBoxMonitor.material-entity-types   |               |                   |      |

#### DSS PROPERTIES


| Key                                                               | Example Value | Short Explanation | More |
|-------------------------------------------------------------------|---------------|-------------------|------|
| dropbox-monitor.dss.reporting-plugins.dropboxReporter.label       |               |                   |      |
| dropbox-monitor.dss.reporting-plugins.dropboxReporter.script-path |               |                   |      |
| dropbox-monitor.dss.reporting-plugins.dropboxReporter.share-id    |               |                   |      |
| root-dir                                                          |               |                   |      |
| dss-registration-log-dir                                          |               |                   |      |

### IMAGING

#### AS PROPERTIES

None.

#### DSS PROPERTIES


| Key                                       | Example Value | Short Explanation | More |
|-------------------------------------------|---------------|-------------------|------|
| imaging.dss.services.imaging.python3-path |               |                   |      |


### MONITORING-SUPPORT

#### AS PROPERTIES

None.

#### DSS PROPERTIES


| Key                                                                                | Example Value | Short Explanation | More |
|------------------------------------------------------------------------------------|---------------|-------------------|------|
| monitoring-support.dss.reporting-plugins.dss-monitoring-initialization.label       |               |                   |      |
| monitoring-support.dss.reporting-plugins.dss-monitoring-initialization.script-path |               |                   |      |
| monitoring-support.dss.reporting-plugins.dss-monitoring-initialization.share-id    |               |                   |      |


### OPENBIS-SYNC

#### AS PROPERTIES

None.

#### DSS PROPERTIES


| Key                                                                                          | Example Value | Short Explanation | More |
|----------------------------------------------------------------------------------------------|---------------|-------------------|------|
| openbis-sync.dss.data-sources.openbis-db.databaseEngineCode                                  |               |                   |      |
| openbis-sync.dss.data-sources.openbis-db.basicDatabaseName                                   |               |                   |      |
| openbis-sync.dss.servlet-services.resource-sync.path                                         |               |                   |      |
| openbis-sync.dss.servlet-services.resource-sync.request-handler                              |               |                   |      |
| openbis-sync.dss.servlet-services.resource-sync.request-handler.file-service-repository-path |               |                   |      |
| openbis-sync.dss.servlet-services.resource-sync.authentication-handler                       |               |                   |      |
| database.kind                                                                                |               |                   |      |
| server-url                                                                                   |               |                   |      |
| download-url                                                                                 |               |                   |      |


### SEARCH-STORE

#### AS PROPERTIES


| Key                                               | Example Value | Short Explanation | More |
|---------------------------------------------------|---------------|-------------------|------|
| search-store.as.services.search-store.script-path |               |                   |      |


#### DSS PROPERTIES

None.

### XLS-IMPORT

#### AS PROPERTIES


| Key                                               | Example Value | Short Explanation | More |
|---------------------------------------------------|---------------|-------------------|------|
| xls-import.as.services.xls-import-api.script-path |               |                   |      |


#### DSS PROPERTIES

None.
















