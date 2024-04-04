# Environments

## Production

### openbis-server - https://hub.docker.com/r/openbis/openbis-server

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/openbis-server)  

The **openbis-server** image is designed and supported for deploying openBIS in production. 
The openBIS service running in container named **openbis-app** can be connected to a [containerized PostgreSQL database](usage.md) or to any managed or cloud-native PostgreSQL service. 
A reverse HTTP proxy is required in front, which can be also [set up as a container](usage.md), as an independent ingress controller, or as a cloud-based content delivery service. 
We recommend to orchestrate all containers using Docker Compose, for which we're providing [examples](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/tree/master/hub/openbis-server/compose) to use as a template. 


## Development

### debian-openbis - https://hub.docker.com/r/openbis/debian-openbis

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/debian-openbis)  

For deveoplment purposes, it is possible to follow the production setup outlined above. To ease up development, rapid testing and debugging, we recommend to use the **debian-openbis** image, which provides an all-in-one container. Note that this setup is **not** optimized for persisting data, and should not be used for running services in production.
