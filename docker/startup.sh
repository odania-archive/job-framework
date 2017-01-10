#!/bin/bash
export HOME=/srv

chown -R jobs:jobs /srv
exec sudo -u jobs -g jobs /opt/job-framework/gradlew docker -Pprod -Dspring.config.location=/srv/application.properties
