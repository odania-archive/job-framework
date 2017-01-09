FROM openjdk:8-jdk-alpine
MAINTAINER Mike Petersen <mike@odania-it.de>

RUN apk add --no-cache bash libstdc++ && rm -rf /var/cache/apk/*

COPY . /opt/job-framework
COPY docker /srv
WORKDIR /opt/job-framework
RUN /opt/job-framework/gradlew assemble

VOLUME ["/srv"]
CMD ["/opt/job-framework/gradlew", "docker", "-Dspring.config.location=/srv/application.properties"]
