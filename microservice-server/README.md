# MICROSERVICE SERVER #

## Introduction ##

This project holds is supposed to be used to developed Java micro services.

## Configuration ##

A json file with the configuration should be given during the startup as an argument, if not given the default "config.json" will be loaded (mostly used for distribution) and if not found "./conf/config.json" is loaded (mostly used for development).

## Build ##

./gradlew distZip

The build will be found at ./build/distributions/microservice-server.zip

## Startup ##

Unzip the build and execute

./bin/microservice-server

## Main packages ##

### ch.ethz.sis.microservices.api ###
Supposed to contain only immutable DTOs annotated with lombok's @Data.

### ch.ethz.sis.microservices.server.json ###
JSON object mapper interface and implementations.

### ch.ethz.sis.microservices.server.logging ###
Logging interface and implementation.

### ch.ethz.sis.microservices.server.services ###
Service interface and implementations.

To implement a new service is sufficient to extend the class ch.ethz.sis.microservices.server.services.Service. This class extends the standard J2EE HttpServlet and is up to the implementer to decide to accept the different http methods.

This class just provides a convenience method getServiceConfig() to retrieve the configuration.

### ch.ethz.sis.microservices.server.startup ###
Main class and launcher.

### ch.ethz.sis.microservices.util ###
Utility classes.

## Services description and configuration ##

The config.json configuration file currently has two sections:
	port : The port where the web server starts.
	services : A list of services.

To configure a service two parameters are mandatory:
	className : Complete Java class name with packages.
	url : URL for the service, since all services share the same web server port.

One parameter is optional:
	parameters : Additional parameters for the service.

``` config.json
{
	"port" : 8080,
	"services" : [
		{ 
			"className" : "ch.ethz.sis.microservices.server.services.store.DownloadHandler", 
			"url" : "/download",
			"parameters" : { 
				"openbis-url" : "http://localhost:8888/openbis/openbis/rmi-application-server-v3",
				"datastore-url" : "http://localhost:8889/datastore_server/rmi-data-store-server-v3",
				"services-timeout" : "10000",
				"allowedExternalDMSCode" : "ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7",
				"allowedContentCopyPath" : "/Users/localadmin/obis_data/"
			}
		} 
	]
}
```

## File Services - (ch.ethz.sis.microservices.server.services.store.FileInfoHandler, ch.ethz.sis.microservices.server.services.store.DownloadHandler) ##
All these handlers share the same configuration and input parameters.

### Configuration ###

openbis-url : Server where is validated that the user sessionToken can retrieve the dataset.
datastore-url : Server where is validated that the user sessionToken can retrieve the file and that the file exists for the given dataset.
services-timeout : Timeout when making calls to openBIS or DSS.
allowedExternalDMSCode : Allowed External DMS code, with the current implementation only one EDMS is possible for the service.
allowedContentCopyPath : Allowed directory where dataset files should be present, trying to access outside of this directory giving relative paths will result in failure.

``` Part of config.json
{	
			"className" : "ch.ethz.sis.microservices.server.services.store.FileInfoHandler", 
			"url" : "/file-information",
			"parameters" : {
				"openbis-url" : "http://localhost:8888/openbis/openbis/rmi-application-server-v3",
				"datastore-url" : "http://localhost:8889/datastore_server/rmi-data-store-server-v3",
				"services-timeout" : "10000",
				"allowedExternalDMSCode" : "ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7",
				"allowedContentCopyPath" : "/Users/localadmin/obis_data/"
			}
}, 
{ 
			"className" : "ch.ethz.sis.microservices.server.services.store.DownloadHandler", 
			"url" : "/download",
			"parameters" : { 
				"openbis-url" : "http://localhost:8888/openbis/openbis/rmi-application-server-v3",
				"datastore-url" : "http://localhost:8889/datastore_server/rmi-data-store-server-v3",
				"services-timeout" : "10000",
				"allowedExternalDMSCode" : "ADMIN-BS-MBPR28.D.ETHZ.CH-E96954A7",
				"allowedContentCopyPath" : "/Users/localadmin/obis_data/"
			}
}
```

### Input parameters ###

The service only accepts GET requests, the GET request should contain 5 mandatory parameters.

sessionToken : User session token
datasetPermId : Dataset perm id the file belongs to, if not found on the AS or DSS an exception is thrown.
externalDMSCode : Should match the configured allowedExternalDMSCode, if not an exception is thrown.
contentCopyPath : Content copy path to access, if not found either AS, DSS or file system an exception is thrown.
datasetPathToFile : File to access, if not found either on the DSS or file system an exception is thrown.

### Output ch.ethz.sis.microservices.server.services.store.FileInfoHandler  ###
The body of the response will contain a JSON map with information from the file.

``` BODY
{
	"isFileAccessible" : "true"
}
```

### Output ch.ethz.sis.microservices.server.services.store.DownloadHandler  ###
The body of the response will contain the raw content from the file.