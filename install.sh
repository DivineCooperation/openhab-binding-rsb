
#!/bin/bash
APP_NAME='openhab-binding-rsb'

clear &&
echo "=== clean ${APP_NAME} ===" &&
mvn clean $@ &&
clear &&
echo "=== install and deploy ${APP_NAME} ===" &&
mvn clean install -Dopenhab.distribution=$prefix/share/openhab/distribution $@ &&
clear &&
echo "=== ${APP_NAME} is successfully installed to ${prefix} ==="

