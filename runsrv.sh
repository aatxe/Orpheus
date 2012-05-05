#!/bin/sh
export CLASSPATH=.:dist/*
java -Xmx64M -Dwzpath=wz/ net.server.Server
