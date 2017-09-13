#!/bin/bash
APP_NAME='openhab-binding-rsb'
clear &&
echo "=== clean ${APP_NAME} ===" &&
mvn clean --quiet $@ &&
clear &&
echo "=== install ${APP_NAME} to ${prefix} ===" &&
mvn install \
        -DassembleDirectory=${prefix} \
        -DskipTests=true \
        -Dmaven.test.skip=true \
        -Dlicense.skipAddThirdParty=true \
        -Dlicense.skipUpdateProjectLicense=true \
        -Dlicense.skipDownloadLicenses \
        -Dlicense.skipCheckLicense=true \
        -Dmaven.license.skip=true \
        -Dopenhab.distribution=$prefix/share/openhab/distribution \
        --quiet $@ &&
clear &&
echo "=== ${APP_NAME} is successfully installed to ${prefix} ==="
