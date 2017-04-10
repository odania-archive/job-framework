#!/usr/bin/env bash

export GRADLE_OPTS=-Dgradle.user.home=/opt/job-framework/.gradle
/opt/job-framework/gradlew assemble

mv /opt/job-framework/build/libs/com-odaniait-job-framework-0.2.0.jar /opt/com-odaniait-job-framework-0.2.0.jar
ln -sf /opt/com-odaniait-job-framework-0.2.0.jar /opt/com-odaniait-job-framework.jar
