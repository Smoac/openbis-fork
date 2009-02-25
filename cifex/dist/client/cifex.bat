@echo off

cifex_root=%~d0%~p0
java -Xmx256m -Djavax.net.ssl.trustStore=etc\keystore -Dcifex.root=%cifex_root% -jar lib\cifex.jar %*
