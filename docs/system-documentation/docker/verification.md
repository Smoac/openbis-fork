# Verification

Check status of running openBIS Application Server.
```
$ docker exec -it openbis-app /home/openbis/servers/openBIS-server/jetty/bin/status.sh;
openBIS Application Server is running (pid 24538)
```

Check version of running openBIS Application Server.
```
$ docker exec -it openbis-app /home/openbis/servers/openBIS-server/jetty/bin/version.sh;
20.10.10 (r1728379762)
```

Check the password file for file based authentication.
```
$ docker exec -it openbis-app /home/openbis/servers/openBIS-server/jetty/bin/passwd.sh list;
User ID               First Name            Last Name             Email
admin
etlserver
```

Check connectivity to port 8080 of openBIS Application Server.
```
$ docker exec -it openbis-app wget -q --output-document - http://localhost:8080/openbis/webapp/eln-lims/version.txt;
20.10.10
```

Examine a process of  openBIS Data Store Server.
```
$ docker exec -it openbis-app pgrep -af DataStoreServer;
438 java -server -Djavax.net.ssl.trustStore=etc/openBIS.keystore --add-exports java.xml/jdk.xml.internal=ALL-UNNAMED -Dnative.libpath=lib/native -classpath lib/slf4j-log4j12-1.6.2.jar:lib/datastore_server.jar:lib/common.jar:lib/dbmigration-20.10.10-r1728379762.jar:lib/activation-1.1.1.jar:lib/ascii-table-1.2.0.jar:lib/aspectjweaver-1.8.12.jar:lib/authentication-20.10.10-r1728379762.jar:lib/autolink-dataset-uploader-api-zip4j_1.3.2.jar:lib/autolink-dropboxReporter-jyson-1.0.2.jar:lib/autolink-eln-lims-api-htmlcleaner-2.23.jar:lib/autolink-eln-lims-api-zip4j_1.3.2.jar:lib/autolink-eln-lims-dropbox-persistentkeyvaluestore.jar:lib/autolink-password-reset-api-persistentkeyvaluestore.jar:lib/autolink-zenodo-exports-api-job-scheduler.jar:lib/base64-2.3.9.jar:lib/bcel-6.0-SNAPSHOT.jar:lib/bcpg-1.59.jar:lib/bcprov-1.59.jar:lib/bioformats-6.5.1.jar:lib/builder-commons-1.0.2.jar:lib/cisd-args4j-9.11.2.jar:lib/cisd-cifex-r1550129411.jar:lib/cisd-hotdeploy-13.01.0.jar:lib/cisd-image-readers-bioformats-r1553067167.jar:lib/cisd-image-readers-imagej-r1553067167.jar:lib/cisd-image-readers-jai-r1553067167.jar:lib/cisd-image-readers-r1553067167.jar:lib/cisd-openbis-knime-server-13.6.0.r29301.jar:lib/classmate-1.3.0.jar:lib/common.jar:lib/commonbase.jar:lib/commons-cli-1.2.jar:lib/commons-codec-1.10.jar:lib/commons-collections-4.01.jar:lib/commons-collections4-4.1.jar:lib/commons-compress-1.8.jar:lib/commons-csv-1.2.jar:lib/commons-dbcp-1.3-CISD.jar:lib/commons-fileupload-1.3.3.jar:lib/commons-io-2.6.jar:lib/commons-lang3-3.11.jar:lib/commons-logging-1.1.1.jar:lib/commons-pool-1.5.6.jar:lib/commons-text-1.6.jar:lib/datastore_server-20.10.10-r1728379762.jar:lib/datastore_server_plugin-dsu-20.10.10-r1728379762.jar:lib/datastore_server_plugin-plasmid-20.10.10-r1728379762.jar:lib/datastore_server_plugin-yeastx-20.10.10-r1728379762.jar:lib/dbmigration-20.10.10-r1728379762.jar:lib/docx4j-6.1.2.jar:lib/dom4j-1.6.1.jar:lib/ehcache-2.10.0.jar:lib/eodsql-2.2-CISD.jar:lib/fast-md5-2.6.1.jar:lib/fontbox-2.0.30.jar:lib/ftpserver-core-1.0.6.jar:lib/graphics2d-3.0.0.jar:lib/guava-25.0-jre.jar:lib/h2-1.1.115.jar:lib/hamcrest-core-1.3.jar:lib/hamcrest-integration-1.3.jar:lib/hamcrest-library-1.3.jar:lib/httpclient-4.3.6.jar:lib/httpcore-4.3.3.jar:lib/ij-1.43u.jar:lib/image-viewer-0.3.6.jar:lib/istack-commons-runtime-3.0.5.jar:lib/jackcess-1.2.2.jar:lib/jackson-annotations-2.9.10.jar:lib/jackson-core-2.9.10.jar:lib/jackson-databind-2.9.10.8.jar:lib/jandex-2.0.3.Final.jar:lib/javacsv-2.0.jar:lib/javassist-3.20.0.GA.jar:lib/javax.annotation-api-1.3.2.jar:lib/javax.jws-3.1.2.2.jar:lib/jaxb-api-2.3.0.jar:lib/jaxb-core-2.3.0.jar:lib/jaxb-runtime-2.3.0.jar:lib/jboss-logging-3.3.0.Final.jar:lib/jboss-transaction-api_1.2_spec-1.0.0.Final.jar:lib/jcommon.jar:lib/jetty-client-9.4.44.v20210927.jar:lib/jetty-deploy-9.4.44.v20210927.jar:lib/jetty-http-9.4.44.v20210927.jar:lib/jetty-io-9.4.44.v20210927.jar:lib/jetty-security-9.4.44.v20210927.jar:lib/jetty-server-9.4.44.v20210927.jar:lib/jetty-servlet-9.4.44.v20210927.jar:lib/jetty-util-9.4.44.v20210927.jar:lib/jetty-webapp-9.4.44.v20210927.jar:lib/jetty-xml-9.4.44.v20210927.jar:lib/jfreechart-1.0.13.jar:lib/jline-0.9.94.jar:lib/jsonrpc4j-1.5.3.jar:lib/jsoup-1.14.2.jar:lib/jython-2.5.2.jar:lib/log4j-1.2.15.jar:lib/mail-1.6.2.jar:lib/marathon-spring-util-1.2.5.jar:lib/mina-core-2.0.7.jar:lib/openbis-20.10.10-r1728379762.jar:lib/openbis-common.jar:lib/openbis-mobile-r29271.jar:lib/openbis_api-20.10.10-r1728379762.jar:lib/openhtmltopdf-core-1.0.10.jar:lib/openhtmltopdf-pdfbox-1.0.10.jar:lib/pdfbox-2.0.30.jar:lib/pdfbox-io-3.0.0.jar:lib/pngj-0.62.jar:lib/poi-3.17.jar:lib/poi-ooxml-3.17.jar:lib/poi-ooxml-schemas-3.17.jar:lib/postgresql-42.5.0.jar:lib/reflections-0.9.10.jar:lib/restrictionchecker-1.0.2.jar:lib/screening-20.10.10-r1728379762.jar:lib/serializer-2.7.2.jar:lib/servlet-api-3.1.0.jar:lib/sis-base-18.09.0.jar:lib/sis-file-transfer-19.03.1.jar:lib/sis-jhdf5-19.04.0.jar:lib/slf4j-1.6.2.jar:lib/slf4j-api-1.7.24.jar:lib/slf4j-log4j12-1.6.2.jar:lib/spring-aop-5.0.17.RELEASE.jar:lib/spring-beans-5.0.17.RELEASE.jar:lib/spring-context-5.0.17.RELEASE.jar:lib/spring-context-support-5.0.17.RELEASE.jar:lib/spring-core-5.0.17.RELEASE.jar:lib/spring-expression-5.0.17.RELEASE.jar:lib/spring-jcl-5.0.17.RELEASE.jar:lib/spring-jdbc-5.0.17.RELEASE.jar:lib/spring-orm-5.0.17.RELEASE.jar:lib/spring-tx-5.0.17.RELEASE.jar:lib/spring-web-5.0.17.RELEASE.jar:lib/spring-webmvc-5.0.1.RELEASE.jar:lib/sshd-common.jar:lib/sshd-core-2.7.0.jar:lib/sshd-sftp-2.7.0.jar:lib/stax-api-1.0.1.jar:lib/stax2-api-3.0.4.jar:lib/truezip-6.8.1.jar:lib/txw2-2.3.0.jar:lib/validation-api-1.0.0.GA.jar:lib/wstx-asl-4.0.0.jar:lib/xalan-2.7.2.jar:lib/xml-apis-1.3.03.jar:lib/xml-io-1.0.3.jar:lib/xmlbeans-2.6.0.jar:lib/xmpbox-2.0.30.jar:lib/xoai-common.jar:lib/xoai-data-provider-4.2.0.jar:ext-lib/*.jar ch.systemsx.cisd.openbis.dss.generic.DataStoreServer
```

Check connectivity to the database.
```
$ docker exec -it openbis-db psql -U openbis openbis_prod -c "select id,user_id,email from persons";
Password for user openbis:
 id |  user_id  | email
----+-----------+-------
  1 | system    |
  2 | etlserver |
  3 | admin     |
(3 rows)
```
