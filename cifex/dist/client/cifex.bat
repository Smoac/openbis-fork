@echo off

java -Xmx256m -Djavax.net.ssl.trustStore=%~dp0etc\keystore -Dcifex.root=%~dp0 -jar %~dp0lib\cifex.jar %*
