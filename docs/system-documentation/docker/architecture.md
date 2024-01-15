# Architecture

![openBIS Architecture](https://gitlab.com/wilkart/openbis/-/raw/master/assets/osgiliath_openbis.png)

# Repositories

## Source code

Source code of builds is public 

## Containers

Container's images are published to Docker Hub.

### Production environments

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/openbis-server)  

### Development environments

![Docker Pulls](https://img.shields.io/docker/pulls/openbis/debian-openbis)  


## Tags

Tags correspond to the major release numbers published on [openBIS download](https://openbis.ch/index.php/downloads/) page. openBIS installation package is deployed on latest official image of [Ubuntu LTS Linux](https://releases.ubuntu.com).  

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



## Containers

Container names, used image and port mappings for the containers.


| Container | Image | Port | Description |
| ----------|------ | ---- | ----------- |
|`openbis-database`|`postgres15`|`5432/tcp`|PostgreSQL database server as a backend. |
|`openbis-server`|`openbis-server`|`8080/tcp`|HTTP protocol of openBIS Application Server as a backend. | 
|`openbis-server`|`openbis-server`|`8081/tcp`|HTTP protocol of openBIS Data Store Server as a backend. |
|`openbis-ingress`|`apache2`|`443/tcp`|HTTP protocol over TLS/SSL as a frontend. |


## Network

Container networking `openbis-tier` refers to the ability for containers to connect to and communicate with each other.

```
$ docker network create openbis-tier --driver bridge;
```



## Volumes

Docker volumes serve as the preferred mechanism for **persisting data** generated and utilized by containers. The data directory of openBIS, main configuration files and logs are defined to be maintained as a persistent volume. By utilizing the option `-v data-openbis:/data`, a persistent storage named `data-openbis` is created and mounted as `/data` within the active container. It analogically applies to all other persistent volumes.

| Container | Persistent volume | Mountpoint | Description |
| --------- | ----------------- | ---------- | ----------- |
|`openbis-database`|`openbis-database-data`|`/var/lib/postgresql/data`|Database data directory [1].|
|`openbis-server`|`openbis-server-data`|`/data`|Application data directory. It contains data store files and other mandatory files to persist data between containers. | 
|`openbis-server`|`openbis-server-etc`|`/etc/openbis`|Application configuration files contain options [2] to persist between containers. Deleted files are restored to default when container start. |
|`openbis-server`|`openbis-server-logs`|`/var/log/openbis`|Application log files. It contains logs and application messages. |

[1] Refer to the [PostgreSQL official image](https://hub.docker.com/_/postgres) to define data backup/restore workflow.  
[2] Refer to the [openBIS documentation](https://openbis.readthedocs.io/en/latest/system-admin-documentation/configuration/index.html) for advanced configuration.  
