# Quickstart

Create virtual network.
```
$ docker network create openbis-network --driver bridge;
```

Run database container.
```
$ docker run --detach \
  --name openbis-db \
  --hostname openbis-db \
  --network openbis-network \
  -v openbis-db-data:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -e PGDATA=/var/lib/postgresql/data/pgdata \
  postgres:15;
```

Run application container.
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
  -e OPENBIS_FQDN="local.openbis.ch" \
  openbis/openbis-app:latest;
```

Run ingress container.
```
$ docker run --detach \
  --name openbis-ingress \
  --hostname openbis-ingress \
  --network openbis-network \
  --pid host \
  -p 443:443 \
  -e OPENBIS_HOST="openbis-app" \
  openbis/openbis-local:latest;
```

Verify connectivity.
```
$ curl -v https://local.openbis.ch/openbis/webapp/eln-lims/version.txt
```
