#!/bin/sh
export CLASSPATH=.:dist/*
java -Xmx10m net.server.CreateINI
