#!/bin/bash

echo Update... &&
git pull &&
echo Start installation routine... &&
mvn clean install -Dopenhab.distribution=$prefix/share/openhab/distribution $@ &&
echo Installation successful.

