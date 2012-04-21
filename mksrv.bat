@echo off
@title MoopleDEV's INI creator
set CLASSPATH=.;dist\*
java -Xmx10m -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.server.CreateINI
pause