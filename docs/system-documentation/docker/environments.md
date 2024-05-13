# Environments

## Production, testing and development

### openbis-app - https://hub.docker.com/r/openbis/openbis-server

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/openbis-server)  

The **openbis-app** image is designed and supported for deploying openBIS in production, testing and development environments. 
The openBIS service running in container named **openbis-app** can be connected to a [containerized PostgreSQL database](usage.md) or to any managed or cloud-native PostgreSQL service. 
A reverse HTTP proxy is required in front. It can be [set up as a container](usage.md), as an independent ingress controller, or as a cloud-based content delivery service. 
We recommend to orchestrate all containers using Docker Compose, for which we're providing [examples](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/tree/master/hub/openbis-app/compose) to use as a template. 
