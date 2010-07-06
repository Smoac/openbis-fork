@echo off

java -Xmx256m -Djavax.net.ssl.trustStore=%~dp0etc\keystore -Dcifex.config=%~dp0\etc -jar %~dp0lib\cifex.jar %*
