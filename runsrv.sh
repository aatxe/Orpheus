#!/bin/sh
export CLASSPATH=.:dist/*
java -Xmx64M -Dwzpath=wz/ -Djavax.net.ssl.keyStore=ks.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=ks.keystore -Djavax.net.ssl.trustStorePassword=passwd net.server.Server