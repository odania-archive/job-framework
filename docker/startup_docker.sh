#!/bin/bash
. /etc/default/docker

ulimit -n 1048576
if [ "$BASH" ]; then
	ulimit -u 1048576
else
	ulimit -p 1048576
fi

exec docker daemon -p "/var/run/docker.pid" 2>&1 > /dev/stdout
