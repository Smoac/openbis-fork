# Configuration properties by module

# CORE MODULES 

## AS  MODULES

### Database Configuration (Required)

| Key                                      | Example Value | Short Explanation                                                                          |
|------------------------------------------|---------------|--------------------------------------------------------------------------------------------|
| database-instance                        | DEFAULT       | The database instance local unique identifier.                                             |
| database.url-host-part                   | localhost     | The host and optionally port.                                                              |
| database.kind                            | prod          |                                                                                            |
| database.owner                           | openbis       | User who owns the database. Default: Operating system user running the server.             |
| database.owner-password                  |               |                                                                                            |
| database.admin-user                      | postgres      | Superuser of the database.                                                                 |
| database.admin-password                  |               |                                                                                            |
| database.max-active-connections          | 20            | Max. number of active database connections.                                                |
| database.max-idle-connections            | 20            | Max. number of idle database connections to keep open.                                     |
| database.active-connections-log-interval | 3600          | Log interval (in seconds) between two regular log entries of the number of active database |

### Session Configuration (Required)

| Key                                            | Example Value                                 | Short Explanation                                                                       |
|------------------------------------------------|-----------------------------------------------|-----------------------------------------------------------------------------------------|
| session-timeout                                | 720                                           | The time after which an inactive session is expired by the service (in minutes).        |
| session-timeout-no-login                       |                                               | Session time (in minutes) in case of presents of file etc/nologin.html. Should be < 30. |
| session-workspace-root-dir                     | ${storeroot-dir}/sessionWorkspace  |                                                                                         |
| max-number-of-sessions-per-user                |                                               |                                                                                         |
| users-with-unrestricted-number-of-sessions     |                                               |                                                                                         |
| personal-access-tokens-enabled                 | true                                          | Enable personal access tokens                                                           |
| personal-access-tokens-file-path               | /home/openbis/run/personal-access-tokens.json |                                                                                         |
| personal-access-tokens-max-validity-period     | 2592000                                       | Validity of personal access tokens                                                      |
| personal-access-tokens-validity-warning-period | 432000                                        |                                                                                         |

### Mail server Configuration (Optional)

| Key                | example Value                    | Short Explanation |
|--------------------|----------------------------------|-------------------|
| mail.from          | openbis@openbis-eln-weis.ethz.ch |                   |
| mail.smtp.host     | localhost                        |                   |
| mail.smtp.password |                                  |                   |
| mail.smtp.user     |                                  |                   |

### Exports Configuration (Optional)

| Key               | Example Value     | Short Explanation                   |
|-------------------|-------------------|-------------------------------------|
| download-url      | https://localhost | The base URL for Web client access. |
| export.data-limit | 10737418240       | Export data limit in bytes          |

### Imports Configuration (Optional)

| Key                          | Example Value                         | Short Explanation                                                                            |
|------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------|
| xls-import.version-data-file | ../../../xls-import-version-info.json | Path to the file which stores version information of master data imported from Excel sheets. |

### Authentication Configuration (Required)

Supported Authentication options are:
- 'file-authentication-service'
- 'ldap-authentication-service'
- 'crowd-authentication-service'
- 'file-crowd-authentication-service'
- 'file-ldap-authentication-service'
- 'stacked-authentication-service' : ldap - crowd

crowd prefixed properties are only used by crowd.
ldap prefixed properties are only used by ldap.

| Key                                        | Example Value               | Short Explanation                                                                                                                                       |
|--------------------------------------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| authentication-service                     | file-authentication-service | Authentication configuration                                                                                                                            |
| user-for-anonymous-login                   |                             | Login of the existing user whose settings will be used for anonymous login                                                                              |
| allow-missing-user-creation                | false                       | When a new person is created in the database the authentication service is asked by default whether this person is known by the authentication service. |
| crowd.service.host                         |                             | Crowd configuration                                                                                                                                     |
| crowd.service.port                         |                             |                                                                                                                                                         |
| crowd.application.name                     |                             |                                                                                                                                                         |
| crowd.application.password                 |                             |                                                                                                                                                         |
| ldap.server.url                            | <LDAP URL1> <LDAP URL2>     | The space-separated URLs of the LDAP servers                                                                                                            |
| ldap.security.principal.distinguished.name |                             | The distinguished name of the security principal                                                                                                        |
| ldap.security.principal.password           |                             |                                                                                                                                                         |
| ldap.security.protocol                     |                             |                                                                                                                                                         |
| ldap.security.authentication-method        |                             |                                                                                                                                                         |
| ldap.referral                              |                             |                                                                                                                                                         |
| ldap.searchBase                            |                             |                                                                                                                                                         |
| ldap.attributenames.user.id                |                             |                                                                                                                                                         |
| ldap.attributenames.email                  |                             |                                                                                                                                                         |
| ldap.attributenames.first.name             |                             |                                                                                                                                                         |
| ldap.attributenames.last.name              |                             |                                                                                                                                                         |
| ldap.queryEmailForAliases                  |                             |                                                                                                                                                         |
| ldap.queryTemplate                         |                             |                                                                                                                                                         |
| ldap.maxRetries                            |                             |                                                                                                                                                         |
| ldap.timeout                               |                             |                                                                                                                                                         |
| ldap.timeToWaitAfterFailure                |                             |                                                                                                                                                         |

### Authorization Configuration (Required)

| Key                                 | Example Value | Short Explanation |
|-------------------------------------|---------------|-------------------|
| authorization.project-level.enabled |               |                   |
| authorization.project-level.users   |               |                   |

### Miscellaneous Configuration (Optional)

| Key                           | Example Value | Short Explanation |
|-------------------------------|---------------|-------------------|
| web-client-configuration-file |               |                   |
| trusted-cross-origin-domains  |               |                   |

### Miscellaneous Configuration (Optional)

| Key                                      | Example Value | Short Explanation |
|------------------------------------------|---------------|-------------------|
| project-samples-enabled                  | true          |                   |
| material-relax-code-constraints          |               |                   |
| data-set-types-with-no-experiment-needed |               |                   |
| create-continuous-sample-codes           |               |                   |

### Support Related Configuration (Optional)

| Key                                  | Example Value | Short Explanation |
|--------------------------------------|---------------|-------------------|
| onlinehelp.generic.root-url          |               | TO_DELETE - the default URL we use, https://wiki-bsse.ethz.ch/display/CISDDoc/OnlineHelp , no longer works         |
| onlinehelp.generic.page-template     | true&fromPageId=40633829 |                   |
| openbis.support.email                |               |                   |
| memorymonitor-monitoring-interval    |               |                   |
| memorymonitor-log-interval           |               |                   |
| memorymonitor-high-watermark-percent |               |                   |


### Miscellaneous Configuration (Optional)

| Key                                      | Example Value                    | Short Explanation                                                                                                                                                                |
|------------------------------------------|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| entity-history.enabled                   | true                             | Enables/disables history of deleted entities. Default value is 'true', keeping entity history has a performance overhead on updates. On certain scenarios you might not want it. |                                                                                                                                                         
| authorization-component-factory          |                                  | Internal - do not change                                                                                                                                                         |
| script-folder                            |                                  | Internal - do not change                                                                                                                                                         |
| jython-version                           | 2.7                              | Internal - do not change                                                                                                                                                         |
| server-public-information.afs-server.url | http://localhost:8085/afs-server | A URL of the AFS server which is used for data storage                                                                                                                           |
| maintenance-plugins                      |                                  | Maintenance plugins configuration                                                                                                                                                |

### V3 API Configuration (Optional)

| Key                                                                                    | Example Value | Short Explanation |
|----------------------------------------------------------------------------------------|---------------|-------------------|
| api.v3.operation-execution.store.path                                                  |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.name                                            |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.core-size                                       |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.max-size                                        |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.thread-pool.keep-alive-time                                 |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.progress.thread-name                                        |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.progress.interval                                           |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.default                                   |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.max                                       |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.summary.default                           |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.summary.max                               |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.details.default                           |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-time.details.max                               |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timeout-pending-task.name          |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timeout-pending-task.interval      |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timed-out-or-deleted-task.name     |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.availability-update.mark-timed-out-or-deleted-task.interval |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.state-update.mark-failed-after-server-restart-task.name     |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.capacity                                              |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.class                                                 |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.directory                                             |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.clearance-task-name                                   |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.timeout                                               |               |                   |                                                                                                                                                         |      |
| api.v3.operation-execution.cache.timeout-check-interval                                |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.enabled                                                             |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.coordinator-key                                                     |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.interactive-session-key                                             |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.transaction-count-limit                                             |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.transaction-timeout                                                 |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.finish-transactions-interval                                        |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.transaction-log-folder-path                                         |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.participant.application-server.url                                  |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.participant.application-server.timeout                              |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.participant.afs-server.url                                          |               |                   |                                                                                                                                                         |      |
| api.v3.transaction.participant.afs-server.timeout                                      |               |                   |                                                                                                                                                         |      |

## DSS MODULES

### Core Configuration (Required)

| Key                                                    | Example Value                                                     | Short Explanation                                                                                                          |
|--------------------------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| data-store-server-code                                 | DSS1                                                              | Unique code of this Data Store Server.                                                                                     |
| host-address                                           | http://localhost                                                  | host name of the machine on which the datastore server is running                                                          |
| root-dir                                               | /home/openbis/data                                                | parent directory of the store directory and all the dropboxes                                                              |
| storeroot-dir                                          | ${root-dir}/store                                                 | The root directory of the data store                                                                                       |
| incoming-root-dir                                      | ${root-dir}                                                       |                                                                                                                            |
| cache-workspace-folder                                 | ${root-dir}/dss-cache                                             | Cache for data set files from other Data Store Servers                                                                     |
| commandqueue-dir                                       | /home/openbis/run/datastore_commandqueue                          | The directory where the command queue file is located                                                                      |
| data-set-command-queue-mapping                         |                                                                   | Comma-separated list of definitions of additional queues for processing processing plugins.                                |
| port                                                   | 8081                                                              |                                                                                                                            |
| use-ssl                                                | false                                                             |                                                                                                                            |
| session-timeout                                        | 720                                                               | Session timeout in minutes                                                                                                 |
| keystore.path                                          | etc/openBIS.keystore                                              | Path to the keystore                                                                                                       |
| keystore.password                                      | changeit                                                          | Password of the keystore                                                                                                   |
| keystore.key-password                                  | changeit                                                          | Key password of the keystore                                                                                               |
| server-url                                             | ${host-address}:8443                                              | The URL of the openBIS server                                                                                              |
| download-url                                           | ${host-address}:${port}                                           | The base URL for Web client access to the data store server.                                                               |
| username                                               | etlserver                                                         | The username to use when contacting the openBIS server                                                                     |
| password                                               |                                                                   | The password for the etlserver user who contacts the openBIS server                                                        |
| check-interval                                         | 5                                                                 | The check interval (in seconds)                                                                                            |
| quiet-period                                           | 180                                                               |                                                                                                                            |
| shutdown-timeout                                       | 180                                                               | The time-out for clean up work in the shutdown sequence (in seconds).                                                      |
| minimum-time-to-keep-streams-in-sec                    | 20                                                                | The minimum time (in seconds) of availability of the data stream since moment when user requested for the data stream url. |
| highwater-mark                                         | -1                                                                | Data Set Registration Halt                                                                                                 |
| recovery-highwater-mark                                | -1                                                                | Data Set Registration Halt                                                                                                 |
| notify-successful-registration                         | false                                                             |                                                                                                                            |
| rsync-options                                          | --no-p --no-o --no-g --chmod=Du=rwx,Fu=rw --chown=openbis:openbis | Typical options to disable coping general, owner and group permissions                                                     |
| api.v3.fast-download.maximum-number-of-allowed-streams | 10                                                                | The maximum number of allowed download streams.                                                                            |
| jython-version                                         | 2.7                                                               |                                                                                                                            |

### Archiver Configuration
| Key                                                    | Example Value                            | Short Explanation                                                                                                          |
|--------------------------------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| archiver.batch-size-in-bytes | 1520000000000 | Size of batches of datasets to be archived. Should be marginally higher than archiver.maximum_container_size_in_bytes |
| archiver.class | ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetArchiver | Archiver class specification |
| archiver.cleaner.deletion-requests-dir | ${storeroot-dir} | Directory pointing to the store from which deletions shall be performed after an archiving request has succeeded |
| archiver.cleaner.email-address |  | Email address that archiver deletion failures should be sent to |
| archiver.cleaner.email-from-address |  | Email address used as the sender address in emails notifying about failed deletions |
| archiver.cleaner.email-subject | MultiDatasetArchiver: Deletion failure | Email subject to be used in emails informing about deletion failures |
| archiver.cleaner.email-template | The following files could not be deleted:\n${file-list} | Template for the email body to be sent in case of deletion failures |
| archiver.cleaner.file-path-prefixes-for-async-deletion |  | Comma-separated list of paths to be used for asynchronous deletions on the remote storage volume. This should match the list of mountpoints specified in archiver.final-destination and archiver.replicated-destination |
| archiver.data-source | multi-dataset-archiver-db | The name of the database to be used by the archiver |
| archiver.database.kind | prod |  |
| archiver.dataset-unarchiving-highwater-mark | | Highwater mark for unarchiving |
| archiver.final-destination |  | Path to the mounted storage volume representing the final (primary) location of the archiver backend (e.g., a mounted tape drive) |
| archiver.finalizer-max-waiting-time | 14 d | Maximum amount of time that the archiver is waiting for an archiving job to complete (both the primary and replica destinations contain a validated copy of the archived data) |
| archiver.finalizer-polling-time | 120 min | Time period the archiver waits after the finalization of the data transfer to the primary destination before checking for t-flags and/or running sanity checks |
| archiver.finalizer-sanity-check	| true | Perform a sanity check after the data to be archived has been copied to the primary destination |
| archiver.finalizer-wait-for-t-flag | true | Wait for the container file (.tar) in the final destination to be flagged with a "t" (used for tape backends that set this flag to indicate that the copying of the data to the actual tape is complete) |
| archiver.hdf5-files-in-data-set | false | Specify whether any of the datasets to be archived may contain HDF5-formatted files. If this is not set, proper archiving of those files will fail. |
| archiver.maximum-container-size-in-bytes | 1500000000000 | Maximum size of container files (tar files) to be created by the archiver |
| archiver.maximum-unarchiving-capacity-in-megabytes | 2000000 | Size of the scratch volume (in MB) to be used for unarchiving data from the archive system |
| archiver.minimum-container-size-in-bytes | 10000000000 | Minimum size of container files (tar files) to be created by the archiver |
| archiver.pause-file-polling-time | 10 min | Time used to polling (querying) the file specified in archiver.pause-file |
| archiver.pause-file | ${root-dir}/pause-archiving | Pause archiving until this file has been removed |
| archiver.replicated-destination |  | Path to the mounted storage volume representing the secondary (replica) location of the archiver backend (e.g., a mounted tape drive) |
| archiver.sql-root-folder | ${root-dir}/sql/multi-dataset-archive |  |
| archiver.staging-destination |  | Path to the mounted storage volume to be used for writing the container file (tar) initially, before moving it to the final destination |
| archiver.timeout | 10800 | Timeout in seconds after which the archiver fails to write data to the final destination |
| archiver.temp-destination |  | Path to the mounted storage volume to be used for storing temporary data during sanity checks |
| archiver.unarchiving-max-waiting-time	| 5 d | Maximum time to wait in between the scheduling of an unarchiving request and the unarchiving request being finished |
| archiver.unarchiving-polling-time	| 1 h | Time used to polling (querying) whether a file on the final destination is flagged as being ready to be unarchived (only used if archiver.unarchiving-wait-for-t-flag
 is set) |
| archiver.unarchiving-prepare-command-template |  | Script to run each archiver.unarchiving-polling-time days/hours/minutes/seconds to trigger the copying of the file from the tape archive to the mounted landing zone that is accessible from the openBIS node |
| archiver.unarchiving-wait-for-t-flag | true | Wait for the container file (.tar) in the final destination to be have the "t" flag removed (used for tape backends that set this flag to indicate that the file has been copied from the actual tape to the mounted landing zone that is accessible from the openBIS node) |
| archiver.unique-experiment-name-property-code | ${experiment-name-property-code} | |
| archiver.unique-sample-name-property-code | ${sample-name-property-code} | |
| archiver.wait-for-sanity-check-initial-waiting-time | 30 min | Initial time to wait beofre attempting to run the sanity check on the final destination for the first time |
| archiver.wait-for-sanity-check-max-waiting-time | 24 h | Maximum time to wait for the sanity check to either return with SUCCESS or ERROR |
| archiver.wait-for-sanity-check | true | Wait for the sanity check to return with SUCCESS (true) or not (false) |
| archiver.waiting-for-free-space-polling-time | 10 min | Time used to polling (querying) whether sufficient disk space got available on the final destination for storing the container file (.tar) |
| archiver.with-sharding | false | Use directory sharding for the data stored on the archiving destination |

### Database Configuration (Required)

| Key                                    | Example Value                                                          | Short Explanation                                            |
|----------------------------------------|------------------------------------------------------------------------|--------------------------------------------------------------|
| data-sources                           | path-info-db                                                           | Data sources                                                 |
| path-info-db.version-holder-class      |                                                                        |                                                              |
| path-info-db.databaseEngineCode        |                                                                        |                                                              |
| path-info-db.basicDatabaseName         |                                                                        |                                                              |
| path-info-db.urlHostPart               |                                                                        |                                                              |
| path-info-db.databaseKind              |                                                                        |                                                              |
| path-info-db.scriptFolder              | ${root-dir}/sql                                                        |                                                              |
| path-info-db.owner                     |                                                                        | The owner of the database                                    |
| path-info-db.password                  |                                                                        | Owner password                                               |
| path-info-db.adminUser                 |                                                                        | The administrator user of the database server.               |
| path-info-db.adminPassword             |                                                                        | Administrator password                                       |

### Mail server Configuration (Optional)

| Key                                                    | Example Value                            | Short Explanation                                                                                                          |
|--------------------------------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| mail.smtp.host                                         |                                          | SMTP properties                                                                                                            |
| mail.from                                              |                                          |                                                                                                                            |
| mail.smtp.user                                         |                                          |                                                                                                                            |
| mail.smtp.password                                     |                                          |                                                                                                                            |

### Mail server Configuration (Optional)

| Key                                                    | Example Value                            | Short Explanation                                                                                                          |
|--------------------------------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| ftp.server.enable                                      | true                                     | When set to 'true' an internal ftp / sftp server will be started.                                                          |
| ftp.server.sftp-port                                   | 2222                                     | SFTP port                                                                                                                  |
| ftp.resolver-dev-mode                                  |                                          |                                                                                                                            |
| ftp.server.certificate                                 |                                          |                                                                                                                            |

### Dropbox Configuration (Optional)

| Key                                                    | Example Value                            | Short Explanation                                                                                                          |
|--------------------------------------------------------|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| inputs                                                 | default-dropbox                          |                                                                                                                            |
| dss-rpc.put-default                                    | default-dropbox                          |                                                                                                                            |
| default-dropbox.incoming-dir                           | ${incoming-root-dir}/incoming-default    |                                                                                                                            |
| default-dropbox.incoming-data-completeness-condition   | auto-detection                           |                                                                                                                            |
| default-dropbox.top-level-data-set-handler             |                                          |                                                                                                                            |
| default-dropbox.program-class                          |                                          |                                                                                                                            |
| default-dropbox.storage-processor                      |                                          |                                                                                                                            |

### Post Registration Task (Optional)

| Key                                                    | Example Value                                                               | Short Explanation                                                                                                          |
|--------------------------------------------------------|-----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| post-registration.class                                | ch.systemsx.cisd.etlserver.postregistration.PostRegistrationMaintenanceTask | Maintenance task for post registration of all paths of a freshly registered data set                                       |
| post-registration.interval                             | 30                                                                          |                                                                                                                            |
| post-registration.cleanup-tasks-folder                 | ${root-dir}/post-registration/cleanup-tasks                                 |                                                                                                                            |
| post-registration.last-seen-data-set-file              | ${root-dir}/post-registration/last-seen-data-set.txt                        |                                                                                                                            |
| post-registration.post-registration-tasks              | pathinfo-feeding                                                            |                                                                                                                            |
| post-registration.pathinfo-feeding.class               | ch.systemsx.cisd.etlserver.path.PathInfoDatabaseFeedingTask                 |                                                                                                                            |
| post-registration.pathinfo-feeding.compute-checksum    | true                                                                        |                                                                                                                            |

### Processing Plugins (Optional)

| Key                                          | Example Value                                                                                                     | Short Explanation                                                                                                     |
|----------------------------------------------|-------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| processing-plugins                           | path-info-db-consistency-check                                                                                    | Comma separated names of processing plugins.                                                                          |
| path-info-db-consistency-check.label         | Path Info DB consistency check                                                                                    | Processing task that checks the consistency between the data store and the meta information stored in the PathInfoDB. |
| path-info-db-consistency-check.dataset-types | .*                                                                                                                |                                                                                                                       |
| path-info-db-consistency-check.class         | ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetAndPathInfoDBConsistencyCheckProcessingPlugin |                                                                                                                       |

### Maintenance Plugins (Optional)

| Key                                    | Example Value                                                          | Short Explanation                                            |
|----------------------------------------|------------------------------------------------------------------------|--------------------------------------------------------------|
| maintenance-plugins                    | post-registration, path-info-deletion                                  | Comma separated names of maintenance plugins.                |
| path-info-deletion.class               | ch.systemsx.cisd.etlserver.plugins.DeleteFromExternalDBMaintenanceTask | Maintenance task for deleting entries from pathinfo database |
| path-info-deletion.interval            | 120                                                                    |                                                              |
| path-info-deletion.data-source         | path-info-db                                                           |                                                              |
| path-info-deletion.data-set-table-name | data_sets                                                              |                                                              |
| path-info-deletion.data-set-perm-id    | CODE                                                                   |                                                              |

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

| Key                                                                        | Example Value | Short Explanation |
|----------------------------------------------------------------------------|---------------|-------------------|
| eln-lims.as.miscellaneous.file-service.file-server.maximum-file-size-in-MB |               |                   |
| eln-lims.as.miscellaneous.file-service.file-server.repository-path         |               |                   |
| eln-lims.as.services.as-eln-lims-api.script-path                           |               |                   |
| eln-lims.as.services.freeze-api.script-path                                |               |                   |
| eln-lims.as.services.publication-api.script-path                           |               |                   |
| eln-lims.as.webapps.eln-lims.webapp-folder                                 |               |                   |
| file-server.section_eln-lims.download-url-template                         |               |                   |
| file-server.section_eln-lims.error-message-template                        |               |                   |

#### DSS PROPERTIES

| Key                                                                                  | Example Value | Short Explanation |
|--------------------------------------------------------------------------------------|---------------|-------------------|
| eln-lims.dss.drop-boxes.eln-lims-dropbox.incoming-data-completeness-condition        |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.top-level-data-set-handler                  |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.script-path                                 |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.storage-processor                           |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.discard-files-patterns                      |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox.illegal-files-patterns                      |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.incoming-data-completeness-condition |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.top-level-data-set-handler           |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.script-path                          |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.storage-processor                    |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.discard-files-patterns               |               |                   |
| eln-lims.dss.drop-boxes.eln-lims-dropbox-marker.illegal-files-patterns               |               |                   |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.resolver-class            |               |                   |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.code                      |               |                   |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.script-file               |               |                   |
| eln-lims.dss.file-system.plugins.eln-tree.resolver-plugins.ftp.resolver-dev-mode     |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.interval                                      |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.dataset-types                                 |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.entity-sequence-properties                    |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.file-types                                    |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.last-seen-data-set-file                       |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.blast-databases-folder                        |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.blast-temp-folder                             |               |                   |
| eln-lims.dss.maintenance-tasks.blastdb.blast-tools-directory                         |               |                   |
| eln-lims.dss.reporting-plugins.archiving-api.sub-directory-name                      |               |                   |
| eln-lims.dss.reporting-plugins.archiving-api.label                                   |               |                   |
| eln-lims.dss.reporting-plugins.dropbox-monitor-api.label                             |               |                   |
| eln-lims.dss.reporting-plugins.dropbox-monitor-api.script-path                       |               |                   |
| eln-lims.dss.reporting-plugins.dropbox-monitor-api.share-id                          |               |                   |
| eln-lims.dss.reporting-plugins.eln-lims-api.label                                    |               |                   |
| eln-lims.dss.reporting-plugins.eln-lims-api.script-path                              |               |                   |
| eln-lims.dss.reporting-plugins.eln-lims-api.share-id                                 |               |                   |
| eln-lims.dss.reporting-plugins.exports-api.label                                     |               |                   |
| eln-lims.dss.reporting-plugins.exports-api.script-path                               |               |                   |
| eln-lims.dss.reporting-plugins.exports-api.limit-data-size-megabytes                 |               |                   |
| eln-lims.dss.reporting-plugins.exports-api.share-id                                  |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.label                              |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.script-path                        |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.max-delay-in-minutes               |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.password-reset-request-subject     |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.password-reset-request-body        |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.new-password-subject               |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.new-password-body                  |               |                   |
| eln-lims.dss.reporting-plugins.password-reset-api.share-id                           |               |                   |
| eln-lims.dss.reporting-plugins.rc-exports-api.label                                  |               |                   |
| eln-lims.dss.reporting-plugins.rc-exports-api.script-path                            |               |                   |
| eln-lims.dss.reporting-plugins.rc-exports-api.realm                                  |               |                   |
| eln-lims.dss.reporting-plugins.rc-exports-api.share-id                               |               |                   |
| eln-lims.dss.reporting-plugins.zenodo-exports-api.label                              |               |                   |
| eln-lims.dss.reporting-plugins.zenodo-exports-api.script-path                        |               |                   |
| eln-lims.dss.reporting-plugins.zenodo-exports-api.share-id                           |               |                   |
| zenodo-exports-api-limit-data-size-megabytes                                         |               |                   |
| zenodo-exports-api-zenodoUrl                                                         |               |                   |
| zenodo-exports-api-accessToken                                                       |               |                   |
| eln-lims.dss.search-domain-services.blastsearch.blast-tools-directory                |               |                   |
| eln-lims.dss.search-domain-services.blastsearch.blast-databases-folder               |               |                   |
| rc-exports-api-limit-data-size-megabytes                                             |               |                   |
| rc-exports-api-service-document-url                                                  |               |                   |
| rc-exports-api-user                                                                  |               |                   |
| rc-exports-api-password                                                              |               |                   |
| dataset-uploader.h5-folders                                                          |               |                   |
| dataset-uploader.h5ar-folders                                                        |               |                   |
| default-incoming-share-id                                                            |               |                   |
| default-incoming-share-minimum-free-space-in-gb                                      |               |                   |
| download-url                                                                         |               |                   |

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

| Key                                                                  | Example Value | Short Explanation |
|----------------------------------------------------------------------|---------------|-------------------|
| dataset-uploader.as.webapps.dataset-uploader.webapp-folder           |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.openbisui-contexts      |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.sample-entity-types     |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.experiment-entity-types |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.label                   |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.sorting                 |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.data-set-entity-types   |               |                   |
| dataset-uploader.as.webapps.dataset-uploader.material-entity-types   |               |                   |

#### DSS PROPERTIES

| Key                                                                     | Example Value | Short Explanation |
|-------------------------------------------------------------------------|---------------|-------------------|
| dataset-uploader.dss.reporting-plugins.dataset-uploader-api.label       |               |                   |
| dataset-uploader.dss.reporting-plugins.dataset-uploader-api.script-path |               |                   |
| dataset-uploader.dss.reporting-plugins.dataset-uploader-api.share-id    |               |                   |
| dataset-uploader.h5-folders                                             |               |                   |
| dataset-uploader.h5ar-folders                                           |               |                   |

### DROPBOX-MONITOR

#### AS PROPERTIES

| Key                                                               | Example Value | Short Explanation |
|-------------------------------------------------------------------|---------------|-------------------|
| dropbox-monitor.as.webapps.dropBoxMonitor.label                   |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.webapp-folder           |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.openbisui-contexts      |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.sample-entity-types     |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.experiment-entity-types |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.sorting                 |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.data-set-entity-types   |               |                   |
| dropbox-monitor.as.webapps.dropBoxMonitor.material-entity-types   |               |                   |

#### DSS PROPERTIES

| Key                                                               | Example Value | Short Explanation |
|-------------------------------------------------------------------|---------------|-------------------|
| dropbox-monitor.dss.reporting-plugins.dropboxReporter.label       |               |                   |
| dropbox-monitor.dss.reporting-plugins.dropboxReporter.script-path |               |                   |
| dropbox-monitor.dss.reporting-plugins.dropboxReporter.share-id    |               |                   |
| root-dir                                                          |               |                   |
| dss-registration-log-dir                                          |               |                   |

### IMAGING

#### AS PROPERTIES

None.

#### DSS PROPERTIES

| Key                                       | Example Value | Short Explanation |
|-------------------------------------------|---------------|-------------------|
| imaging.dss.services.imaging.python3-path |               |                   |

### MONITORING-SUPPORT

#### AS PROPERTIES

None.

#### DSS PROPERTIES

| Key                                                                                | Example Value | Short Explanation |
|------------------------------------------------------------------------------------|---------------|-------------------|
| monitoring-support.dss.reporting-plugins.dss-monitoring-initialization.label       |               |                   |
| monitoring-support.dss.reporting-plugins.dss-monitoring-initialization.script-path |               |                   |
| monitoring-support.dss.reporting-plugins.dss-monitoring-initialization.share-id    |               |                   |

### OPENBIS-SYNC

#### AS PROPERTIES

None.

#### DSS PROPERTIES

| Key                                                                                          | Example Value | Short Explanation |
|----------------------------------------------------------------------------------------------|---------------|-------------------|
| openbis-sync.dss.data-sources.openbis-db.databaseEngineCode                                  |               |                   |
| openbis-sync.dss.data-sources.openbis-db.basicDatabaseName                                   |               |                   |
| openbis-sync.dss.servlet-services.resource-sync.path                                         |               |                   |
| openbis-sync.dss.servlet-services.resource-sync.request-handler                              |               |                   |
| openbis-sync.dss.servlet-services.resource-sync.request-handler.file-service-repository-path |               |                   |
| openbis-sync.dss.servlet-services.resource-sync.authentication-handler                       |               |                   |
| database.kind                                                                                |               |                   |
| server-url                                                                                   |               |                   |
| download-url                                                                                 |               |                   |

### SEARCH-STORE

#### AS PROPERTIES

| Key                                               | Example Value | Short Explanation |
|---------------------------------------------------|---------------|-------------------|
| search-store.as.services.search-store.script-path |               |                   |

#### DSS PROPERTIES

None.

### XLS-IMPORT

#### AS PROPERTIES

| Key                                               | Example Value | Short Explanation |
|---------------------------------------------------|---------------|-------------------|
| xls-import.as.services.xls-import-api.script-path |               |                   |

#### DSS PROPERTIES

None.
















