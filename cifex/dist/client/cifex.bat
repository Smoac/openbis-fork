@echo off


java -Xmx256m -Djavax.net.ssl.trustStore=%~d0%~p0\etc\keystore -Dcifex.root=%~d0%~p0 -jar %~d0%~p0\lib\cifex.jar %*
