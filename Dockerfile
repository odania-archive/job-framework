FROM openjdk:8-jdk-alpine
MAINTAINER Mike Petersen <mike@odania-it.de>

RUN apk add --no-cache bash libstdc++ sudo && rm -rf /var/cache/apk/*

COPY . /opt/job-framework
COPY docker/data /srv
COPY docker/startup.sh /startup.sh
WORKDIR /opt/job-framework
RUN /opt/job-framework/gradlew assemble

RUN adduser -h /srv -s /bin/bash -D -u 1000 jobs
RUN chown -R jobs:jobs /srv

VOLUME ["/srv"]
CMD ["/startup.sh"]
