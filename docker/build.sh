#!/usr/bin/env bash

export GRADLE_OPTS=-Dgradle.user.home=/opt/job-framework/.gradle
/opt/job-framework/gradlew assemble

mv /opt/job-framework/build/libs/com-odaniait-job-framework-*.jar /opt/com-odaniait-job-framework.jar
[ ! -f "/opt/com-odaniait-job-framework.jar" ] && { echo "Error: /opt/com-odaniait-job-framework.jar does not exist!"; exit 2; }
[ ! -s "/opt/com-odaniait-job-framework.jar" ] && { echo "Error: /opt/com-odaniait-job-framework.jar is empty!"; ls -lha /opt/; exit 2; }
echo jobs:jobs /opt/com-odaniait-job-framework.jar

