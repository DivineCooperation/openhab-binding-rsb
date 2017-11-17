#!/bin/bash

echo '[INFO] try to identify openhab location...'

if [ ! -d "$openhab_distribution" ]; then
    echo [ERROR] openhab distribution [$openhab_distribution] not found!
    echo [INFO] please refer the distribution folder by calling "mvn install -Dopenhab.distribution=path_to_dist"
    echo [WARN] skip binding deploy process...
    exit ;

fi

#echo [INFO] use distribution $openhab_distribution
#echo [INFO] deploy binding into openhab distribution...

scp -r target/*jar $openhab_distribution/addons/

