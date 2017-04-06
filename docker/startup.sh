#!/bin/bash
export HOME=/srv
exec java -jar /opt/com-odaniait-job-framework.jar --spring.config.location=/srv/application.properties
