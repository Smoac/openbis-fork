# MONITOR TOOL #

## Introduction ##

This tool monitors openBIS is up making a series of API calls.

It logs in on the AS, fetches a Dataset definition from the AS and returns a file listings from the DSS.

## Build ##

./gradlew distZip

The build will be found at ./build/distributions/openbis_monitor_tool.zip

## Usage ##

1. Unzip the zip file.
2. From the root folder of the decompressed zip call the script on the bin directory.
3. Example of expected output.

```
openbis-monitor-tool % ./bin/openbis-monitor-tool https://openbis-eln.ethz.ch/ 30000 user_id user_password 20150407224113301-26972
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Login [AS]: true
Dataset Search [AS]: true
File Search [DSS]: true
ALL OK: true
```


