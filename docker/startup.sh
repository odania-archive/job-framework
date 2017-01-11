#!/bin/bash
export HOME=/srv
chown -R jobs:jobs /srv
exec java -jar /opt/com-odaniait-job-framework.jar --spring.config.location=/srv/application.properties
