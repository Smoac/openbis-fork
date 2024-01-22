# Architecture

Docker is a platform that enables **openBIS service** inside lightweight, portable containers. Containers are a form of virtualization that allows applications and their dependencies to be packaged together, ensuring consistency across different environments.

![openBIS Docker User](../../_static/docker_openbis_user.svg)


## Containers

The containerized example of openBIS service is divided into **three containers**. 1) **openbis-database** runs Relational Database Management System for index information, metadata and selected search results. 2) **openbis-server** runs Java Runtime Environment with openBIS Application Server and openBIS Data Store Server, 3) **openbis-ingress** runs Reverse HTTP Proxy for HTTP requests management and Transport Layer Security implementation and termination.

| Container | Image | Port | Description |
| ----------|------ | ---- | ----------- |
|`openbis-database`|`postgres15`|`5432/tcp`|PostgreSQL database listens on port 5432 and accepts connection from openbis-server.|
|`openbis-server`|`openbis-server`|`8080/tcp`|Java Virtual Machine with openBIS Application Server listens on port 8080.| 
|`openbis-server`|`openbis-server`|`8081/tcp`|Java Virtual Machine with openBIS Data Store Server listens on port 8081.|
|`openbis-ingress`|`apache2`|`443/tcp`|Apache HTTP server listens on port 443 and is configured as reverse proxy to ports 8080 and 8081.|


## Tags

Tag names correspond to the major release numbers published on [openBIS download](https://openbis.ch/index.php/downloads/) with latest bugfix patches included. Official openBIS installation package is deployed on latest official image of [Ubuntu LTS Linux](https://releases.ubuntu.com). All containers are rebuilt and republished at least in monthly basis to include latest patches from openBIS release as well as patches from Ubuntu release.

**20.10.7**  
![Docker Version](https://img.shields.io/docker/v/openbis/openbis-server/20.10.7)
![Docker Image Size](https://img.shields.io/docker/image-size/openbis/openbis-server/20.10.7?arch=amd64)

**20.10.6**  
![Docker Version](https://img.shields.io/docker/v/openbis/openbis-server/20.10.6)
![Docker Image Size](https://img.shields.io/docker/image-size/openbis/openbis-server/20.10.6?arch=amd64)

**20.10.5**  
![Docker Version](https://img.shields.io/docker/v/openbis/openbis-server/20.10.5)
![Docker Image Size](https://img.shields.io/docker/image-size/openbis/openbis-server/20.10.5?arch=amd64)

**20.10.4**  
![Docker Version](https://img.shields.io/docker/v/openbis/openbis-server/20.10.4)
![Docker Image Size](https://img.shields.io/docker/image-size/openbis/openbis-server/20.10.4?arch=amd64)


## Network

Container networking `openbis-tier` refers to the ability for containers to connect to and **communicate with each other**. The following example creates a network using the bridge network driver. Running containers will be communicating accross the created virtual network.

```
$ docker network create openbis-tier --driver bridge;
```


## Storage Volumes

Docker storage volumes serve as the preferred mechanism for **persisting data** generated and utilized by containers. The data directory of openBIS, main configuration files and logs are defined to be maintained as a persistent volume. By utilizing the option `-v openbis-data:/data`, a persistent storage named `openbis-data` is created and mounted as `/data` within the active container. It analogically applies to all other persistent volumes.

| Container | Persistent volume | Mountpoint | Description |
| --------- | ----------------- | ---------- | ----------- |
|`openbis-database`|`openbis-database-data`|`/var/lib/postgresql/data`|PostgreSQL database configuration and data directory.|
|`openbis-server`|`openbis-server-data`|`/data`|Application data directory for data store files to persist data between containers.| 
|`openbis-server`|`openbis-server-etc`|`/etc/openbis`|Application configuration files to persist configuration between containers.|
|`openbis-server`|`openbis-server-logs`|`/var/log/openbis`|Application log files to persist logs and application messages.|


# Repositories

## Source code

Source code of all builds and helper scripts is published in [openBIS Continous Integration repository](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/tree/master/hub). It is only official location of source code supported by openBIS team of ETH Zurich Scientific IT Services.


## Containers

Container images are published to [Docker Hub repository](https://hub.docker.com/u/openbis). Builds are executed according to a monthly schedule to include latest openBIS and Ubuntu Linux bugfixes and patches. It is only official location of images supported by openBIS team of ETH Zurich Scientific IT Services. 


# Environments

## Production

### openbis-server - https://hub.docker.com/r/openbis/openbis-server

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/openbis-server)  

openBIS for production is recommended to be setup on **openbis-server** container. openBIS service can be connected to PostgreSQL database run in container ([see Usage](usage.md)) or to managed or cloud native PostgreSQL service. Reverse HTTP Proxy can be setup as a container ([see Usage](usage.md)), as a independent ingress controller or as a cloud based content delivery service.


## Development

### debian-openbis - https://hub.docker.com/r/openbis/debian-openbis

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/debian-openbis)  

openBIS for development can be setup as a production described above. For advanced cases it can be setup on "all in one" **debian-openbis** container. This container is organized to facilitate easy development, rapid testing and debugging where data persistance and production release life cycle are not in concern.
