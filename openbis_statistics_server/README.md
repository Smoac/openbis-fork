# OPENBIS STATISTICS SERVER #

## Introduction ##

This project contains the statistics server micro services.

## Configuration ##

A json file with the configuration should be given during the startup as an argument. If it os not given the default "config.json" will be loaded (mostly used for distribution) and if not found "./conf/config.json" is loaded (mostly used for development).

## Build ##

./gradlew distZip

The build will be found at ./build/distributions/openbis_statistics_server.zip

## Startup ##

Unzip the build and execute

./bin/openbis_statistics_server

## Services description and configuration ##

The config.json configuration file currently has two services:

Only the "statisticsFolderPath" needs to be configured.

```
{
	"port" : 8080,
	"services" : [
		{
			"className" : "ch.ethz.sis.microservices.download.server.services.store.StatisticsReceiverHandler",
			"url" : "/statistics",
			"parameters" : {
				"statisticsFolderPath" : "/Users/localadmin/obis_data"
			}
		},
		{
			"className" : "ch.ethz.sis.microservices.download.server.services.store.PingHandler",
			"url" : "/ping"
		}
	]
}
```