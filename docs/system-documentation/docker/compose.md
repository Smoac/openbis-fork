# Docker Compose

Docker Compose is a tool for defining and running multi-container applications. 
Compose simplifies the control of the entire openBIS service, making it easy to manage in a single, comprehensible YAML configuration file. 
Then, with a single command, you create and start all the services from your configuration file.

```
networks:
  openbis-tier:

volumes:
  openbis-database-data:
  openbis-server-data:
  openbis-server-etc:
  openbis-server-logs:

services:
  openbis-database:
    hostname: openbis-database
    image: postgres:15
    environment:
      - POSTGRES_PASSWORD="mysecretpassword"
      - PGDATA="/var/lib/postgresql/data/pgdata"
      - POSTGRES_HOST_AUTH_METHOD="trust"
    volumes:
      - pg-data:/var/lib/postgresql/data
    networks:
      - openbis-tier
  openbis-server:
    hostname: openbis-server
    image: openbis/openbis-server:20.10.7
    depends_on:
      - openbis-database
    environment:
      - OPENBIS_ADMIN_PASS="123456789"
      - OPENBIS_DATA="/data/openbis"
      - OPENBIS_DB_ADMIN_PASS="mysecretpassword"
      - OPENBIS_DB_ADMIN_USER="postgres"
      - OPENBIS_DB_APP_PASS="mysecretpassword"
      - OPENBIS_DB_APP_USER="openbis"
      - OPENBIS_DB_HOST="openbis-database"
      - OPENBIS_ETC="/etc/openbis"
      - OPENBIS_HOME="/home/openbis"
      - OPENBIS_LOG="/var/log/openbis"
      - OPENBIS_FQDN="openbis.domain"
    volumes:
      - openbis-server-data:/data
      - openbis-server-etc:/etc/openbis
      - openbis-server-logs:/var/log/openbis
    ports:
      - 8081:8081
      - 8080:8080
    networks:
      - openbis-tier
```
