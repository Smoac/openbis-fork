# Usage

## Docker Containers

Our recommendation is to run openBIS within a **three-container setup**, in particular when aiming at [running openBIS in production](environments.md#production):
1) **openbis-ingress**: Runs a reverse HTTP Proxy for managing and securing HTTP requests in between the client and the application.
2) **openbis-app**: Runs a Java Runtime Environment, including the openBIS Application Server (AS) and openBIS Data Store Server (DSS).
3) **openbis-db**: Runs a [PostgreSQL](https://www.postgresql.org/about/) database, to handle all data transactions.


| Container | Image | Port | Description |
| ----------|------ | ---- | ----------- |
|`openbis-db`|`postgres15`|`5432/tcp`|PostgreSQL database listens on port 5432 and accepts connection from openbis-app.|
|`openbis-app`|`openbis-server`|`8080/tcp`|Java Virtual Machine with openBIS Application Server listens on port 8080.| 
|`openbis-app`|`openbis-server`|`8081/tcp`|Java Virtual Machine with openBIS Data Store Server listens on port 8081.|
|`openbis-ingress`|`apache2`|`443/tcp`|Apache HTTP server listens on port 443 and is configured as reverse proxy to ports 8080 and 8081.|


## Docker Compose

Docker Compose is a tool for defining and running multi-container applications. It simplifies the control of the entire openBIS service, making it easy to control the application workflow in a single, comprehensible YAML configuration file, allows to create, start and stop all services by issuing a single command.

We are providing a pre-compiled `docker-compose.yml` available for download [here](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/blob/04c237df3a7c92c75c9c64242ddfa9dc150d72f7/hub/openbis-server/compose/docker-compose.yml), which is ready to use out of the box. To run the application, just navigate to the sub-directory where you've downloaded the `docker-compose.yml` to, and then run `docker-compose up -d` (exact command depends on your OS, and the release of docker and docker-compose installed). For advanced use, consider to modify the file according to your needs ([more details](usage.md)). Note that this example does not include an ingress controler. For full examples, proceed to [Ingress](#ingress).

The sections below provide a brief description of the individual components used in the proposed multi-container setup.


## Docker Network

The virtual bridge network `openbis-network` allows all containers deployed with openBIS to connect to each other. The following example creates a network using the bridge network driver, which all running containers will be communicating accross.

To manually create the network, use: `docker network create openbis-network --driver bridge`


## Storage Volumes

The use of Docker volumes is preferred for **persisting data** generated and utilized by containers. For proper operation, the data directory of openBIS, main configuration files and logs are to be mounted as  persistent volumes. This ensures that data can be accessed from different containers, and allows data to persist container restarts. By utilizing the option `-v openbis-data:/data`, a persistent storage named `openbis-data` is created and mounted as `/data` within the active container. This applies analogically to all other persistent volumes.

| Container | Persistent volume | Mountpoint | Description |
| --------- | ----------------- | ---------- | ----------- |
|`openbis-db`|`openbis-db-data`|`/var/lib/postgresql/data`|PostgreSQL database configuration and data directory.|
|`openbis-app`|`openbis-app-data`|`/data`|Application data directory for data store files.| 
|`openbis-app`|`openbis-app-etc`|`/etc/openbis`|Application configuration files.|
|`openbis-app`|`openbis-app-logs`|`/var/log/openbis`|Application log files.|


## Database

The **database container** `openbis-db` provides a relational database through **PostgreSQL server** to guarantee persistence for any data created while running openBIS. This includes user and authorization data, openBIS entities and their metadata, as well as index information about all datasets. It is required to have database superuser privileges.

```
$ docker run -d \
  --name openbis-db \
  --hostname openbis-db \
  --network openbis-network \
  -v openbis-db-data:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -e PGDATA=/var/lib/postgresql/data \
  postgres:15;
```

The running database container can be inspected to fetch logs. The database has started up successfully when the openbis-db container logs the following message: "database system is ready to accept connections":

```
$ docker logs openbis-db;
2024-01-19 18:37:50.984 UTC [1] LOG:  database system is ready to accept connections
```

The volume created (`openbis-db-data`) can be inspected to check the mountpoint where the database data is physically stored.

```
$ docker volume inspect openbis-db-data;
[
    {
        "CreatedAt": "2024-01-19T19:37:48+01:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/openbis-db-data/_data",
        "Name": "openbis-db-data",
        "Options": null,
        "Scope": "local"
    }
]
```


## Application

The **application container** `openbis-app` provides Java runtime and consists of two Java processes - the **openBIS Application Server** (openBIS AS) and the - **openBIS Data Store Server** (openBIS DSS). The **openBIS AS** manages the metadata and links to the data, while the **openBIS DSS** manages the data themselves operating on a managed part of the file system.

```
$ docker run --detach \
  --name openbis-app \
  --hostname openbis-app \
  --network openbis-network \
  --pid host \
  -p 8080:8080 \
  -p 8081:8081 \
  -v openbis-app-data:/data \
  -v openbis-app-etc:/etc/openbis \
  -v openbis-app-logs:/var/log/openbis \
  -e OPENBIS_ADMIN_PASS="123456789" \
  -e OPENBIS_DATA="/data/openbis" \
  -e OPENBIS_DB_ADMIN_PASS="mysecretpassword" \
  -e OPENBIS_DB_ADMIN_USER="postgres" \
  -e OPENBIS_DB_APP_PASS="mysecretpassword" \
  -e OPENBIS_DB_APP_USER="openbis" \
  -e OPENBIS_DB_HOST="openbis-db" \
  -e OPENBIS_ETC="/etc/openbis" \
  -e OPENBIS_HOME="/home/openbis" \
  -e OPENBIS_LOG="/var/log/openbis" \
  -e OPENBIS_FQDN="openbis.domain" \
  openbis/openbis-server:20.10.7;
```

The state of the running application container can be inspected by fetching the container logs:

```
$ docker logs openbis-app;
2024-01-23 11:06:19,310 INFO  [main] OPERATION.ETLDaemon - Data Store Server ready and waiting for data.
```

Docker volumes mounted by `openbis-app` can be inspected to check where data files, configuration files and logs are physically stored.

```
$ docker volume inspect openbis-app-data openbis-app-etc openbis-app-logs;
[
    {
        "CreatedAt": "2024-01-20T12:24:49+01:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/openbis-app-data/_data",
        "Name": "openbis-app-data",
        "Options": null,
        "Scope": "local"
    },
    {
        "CreatedAt": "2024-01-20T12:24:49+01:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/openbis-app-etc/_data",
        "Name": "openbis-app-etc",
        "Options": null,
        "Scope": "local"
    },
    {
        "CreatedAt": "2024-01-20T12:24:49+01:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/openbis-app-logs/_data",
        "Name": "openbis-app-logs",
        "Options": null,
        "Scope": "local"
    }
]
```


## Ingress

An **ingress container** acts as reverse proxy and performs Transport Layer Security (TLS) termination. The examples provided below only cover the base functionality. They should be extended to handle complex access control scenarios and to comply with firewall rules. In each of the examples below, the ingress controller configures TLS, and it is configured as a reverse proxy to handle requests to the path `/openbis` (directed to port 8080) and to `/datastore_server` (directed to port 8081).

### Nginx

In order to use nginx as an ingress container, it is required to deploy the following files, as provided on our [source-repositories.md](source repository):
- [docker-compose-nginx.yml](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/blob/945c5cbc1925b9777b7ae05acd6ee61b00e7f2b0/hub/openbis-server/compose/docker-compose-nginx.yml)
- [nginx config](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/blob/945c5cbc1925b9777b7ae05acd6ee61b00e7f2b0/hub/openbis-server/compose/nginx/my-nginx.conf), to be placed in sub-directory `nginx`

To run the application, you need to:
- have docker and docker-compose installed
- ensure that valid certificate and key files are deployed in the sub-directory `certs`
- from within the directory where you've deployed the `docker-compose-nginx.yml`, run `docker-compose -f docker-compose-nginx.yml up -d`

### Apache httpd

In order to use apache-httpd as an ingress container, it is required to deploy the following files, as provided on our [source-repositories.md](source repository):
- [docker-compose-httpd.yml](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/blob/945c5cbc1925b9777b7ae05acd6ee61b00e7f2b0/hub/openbis-server/compose/docker-compose-httpd.yml)
- [apache-httpd config](https://sissource.ethz.ch/sispub/openbis-continuous-integration/-/blob/945c5cbc1925b9777b7ae05acd6ee61b00e7f2b0/hub/openbis-server/compose/httpd/my-httpd.conf), to be placed in sub-directory `httpd`

To run the application, you need to:
- have docker and docker-compose installed
- ensure that valid certificate and key files are deployed in the sub-directory `certs`
- from within the directory where you've deployed the `docker-compose-httpd.yml`, run `docker-compose -f docker-compose-httpd.yml up -d`

### HAProxy

Easily functional example for HAProxy.

```
    global
        maxconn 1024
        chroot /var/haproxy
        uid 604
        gid 604
        daemon
        pidfile /var/run/haproxy.pid
        ssl-default-bind-ciphers ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:RSA+AESGCM:RSA+AES:!aNULL:!MD5:!DSS
        ssl-default-bind-options no-sslv3
        tune.ssl.default-dh-param 2048

    defaults
        log global
        mode http
        option httplog
        option dontlognull
        option redispatch
        retries 3
        maxconn 2000

    frontend openbis_ingress
        bind *:443 ssl crt /etc/haproxy/ssl
        acl is_as path_beg /openbis
        acl is_dss path_beg /datastore_server
        use_backend openbis_as if is_as
        use_backend openbis_dss if is_dss
        default_backend openbis_as

    backend openbis_as
        option forwardfor
        server as openbis-app:8080 check

     backend openbis_dss
        option forwardfor
        server dss openbis-app:8081 check
```
