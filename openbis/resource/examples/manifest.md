Examples
========

This folder contains several example core-plugins technologies that illustrate different aspects of openBIS functionality. This file explains what these examples do.

service-demo
============

The service-demo example core-plugin technology demonstrates the integration between openBIS webapps and aggregation and ingestion services. 

Services
--------

The example includes  an aggregation service, example-aggregation-service, and an ingestion service, example-ingestion-service. The example-aggregation-service returns a table that contains the parameters pass in the by the caller. 

The example-ingestion-service registers a sample of type `LIBRARY`. This script assumes that there is an experiment with the identifier `/TEST/TEST-PROJECT/DEMO-EXP-HCS` and a sample type `LIBRARY`. This experiment and sample type are automatically installed by the openBIS installer in a fresh installation. If they do not exist in your installation, you will need to change the script to register a sample type that exists and associate it with an experiment that exists.

Note, by default, the `LIBRARY` sample type is not shown in the standard sample lists. To see the registered samples, you will need to explicitly search for it. This can be done by entering "LIBRARY" in the openBIS search box.

Webapps
-------

This example includes two webapps, `aggregation` and `ingestion`. The aggregation webapp calls the example-aggregation-service and displays the result. The ingestion webapp calls the example-ingestion-service, specifying a sample code determined by the the current time, and displays the result.

The aggregation webapp is available at the url: {server url}/aggregation (e.g., https://localhost:8443/aggregation/ for a local installation).
The ingestion webapp is available at the url: {server url}/ingestion (e.g., https://localhost:8443/ingestion/ for a local installation).

