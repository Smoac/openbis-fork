# Usage

## Database

**Database container** provides relational database (PostgreSQL) server to persist users, authorization information, various entities and their metadata, as well as index information about all datasets. It is required to have database superuser privileges.

```
$ docker run -d \
  --name openbis-database \
  --hostname openbis-database \
  --network openbis-tier \
  -v openbis-database-data:/var/lib/postgresql/data \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -e PGDATA=/var/lib/postgresql/data/pgdata \
  postgres:15;
```

## Application

**Application container** consists of two servers, the openBIS Application Server (AS) and the openBIS Data Store Server (DSS). openBIS AS manages the metadata and links to the data while the openBIS DSS manages the data itself operating on a managed part of the file system.

```
$ docker run --detach \
  --name openbis-server \
  --hostname openbis-server \
  --network openbis-tier \
  --pid host \
  -p 8080:8080 \
  -p 8081:8081 \
  -v openbis-server-data:/data \
  -v openbis-server-etc:/etc/openbis \
  -v openbis-server-logs:/var/log/openbis \
  -e OPENBIS_ADMIN_PASS="123456789" \
  -e OPENBIS_DATA="/data/openbis" \
  -e OPENBIS_DB_ADMIN_PASS="mysecretpassword" \
  -e OPENBIS_DB_ADMIN_USER="postgres" \
  -e OPENBIS_DB_APP_PASS="mysecretpassword" \
  -e OPENBIS_DB_APP_USER="openbis" \
  -e OPENBIS_DB_HOST="postgres15" \
  -e OPENBIS_ETC="/etc/openbis" \
  -e OPENBIS_HOME="/home/openbis" \
  -e OPENBIS_LOG="/var/log/openbis" \
  openbis/openbis-server:20.10.7;
```

## Ingress

**Ingress container** provides TLS termination and reverse proxy. It can be used for complex access control or web application firewall. Examples below are all functional. They configure Transport Layer Security, and reverse proxy based on path, where "/openbis" go to port 8080, and "/datastore_server" go to port 8081. 

### Nginx

Minimal functional example of server block which can be used as a template for the final configurations. 

```
    server {
        listen 443 ssl;
        listen [::]:443 ssl;
        server_name openbis.domain;
        root /var/www/html;

        location /openbis/ {
          proxy_set_header Host $host;
          proxy_set_header X-Real-IP $remote_addr;
          proxy_pass http://openbis:8080;
        }

        location /datastore_server/ {
          proxy_set_header Host $host;
          proxy_set_header X-Real-IP $remote_addr;
          proxy_pass http://openbis:8081;
        }

        ssl_certificate /etc/ssl/openbis.domain.pem;
        ssl_certificate_key /etc/ssl/private/openbis.domain.key;

        ssl_session_timeout 5m;
        ssl_session_cache shared:SSL:1m;
        ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5:!RC4;
    }
```

### HAProxy

Minimal functional example which can be used as a template for the final configuration.

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
        server as openbis:8080 check

     backend openbis_dss
        option forwardfor
        server dss openbis:8081 check
```

### Apache httpd

Minimal functional example of VirtualHost which can be used as a template for the final configuration.

```
    <VirtualHost _default_:443>
        ServerName openbis.domain
        DocumentRoot "/var/www/html"

        SSLEngine on
        SSLCertificateFile /etc/ssl/openbis.domain.pem
        SSLCertificateKeyFile /etc/ssl/private/openbis.domain.key
        SSLProtocol all -SSLv2
        SSLCipherSuite ALL:!ADH:!EXPORT:!SSLv2:RC4+RSA:+HIGH:+MEDIUM:+LOW

        SSLProxyEngine on
        SSLProxyCheckPeerCN off
        SSLProxyCheckPeerExpire off
        ProxyRequests off
        ProxyPreserveHost on

        AllowEncodedSlashes on

        RewriteEngine on
        RewriteRule ^/openbis$ /openbis/ [R,L]
        RewriteRule ^/datastore_server$ /datastore_server/ [R,L]

        ProxyPass /openbis/ http://openbis:8080/openbis/ timeout=600 keepalive=off
        ProxyPassReverse /openbis/ http://openbis:8080/openbis/
        ProxyPass /datastore_server/ http://openbis:8081/datastore_server/
        ProxyPassReverse /datastore_server/ http://openbis:8081/datastore_server/
    </VirtualHost>
```
