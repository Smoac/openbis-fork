@echo off

java -Xmx256m -Dcifex.config=%~dp0\etc -jar %~dp0lib\cifex.jar %*
