#!/bin/sh
export CLASSPATH=.:dist/*
java -Xmx10m -Djavax.net.ssl.eyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.server.CreateINI
