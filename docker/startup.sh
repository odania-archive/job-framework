#!/bin/bash
export HOME=/srv

if [[ "${START_DOCKER}" == "true" ]]; then
	/startup_docker.sh &
fi

chown -R jobs:jobs /srv
exec sudo -u jobs -g jobs /opt/job-framework/gradlew docker -Dspring.config.location=/srv/application.properties
