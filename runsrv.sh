#!/bin/sh
export CLASSPATH=.:dist/*
java -Xmx100M -Dwzpath=wz/ net.server.Server
