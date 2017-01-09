#!/bin/bash
export HOME=/srv

if [[ "${START_DOCKER}" == "true" ]]; then
	. /etc/default/docker

	ulimit -n 1048576
	if [ "$BASH" ]; then
		ulimit -u 1048576
	else
		ulimit -p 1048576
	fi

	exec docker daemon -p "/var/run/docker.pid" 2>&1 > /dev/stdout &
fi

chown -R jobs:jobs /srv
exec sudo -u jobs -g jobs /opt/job-framework/gradlew docker -Dspring.config.location=/srv/application.properties
