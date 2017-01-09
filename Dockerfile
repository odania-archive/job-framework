FROM openjdk:8-jdk-alpine
MAINTAINER Mike Petersen <mike@odania-it.de>

RUN apk update && apk --no-cache add vim curl autoconf zlib-dev unzip bzip2 ca-certificates libffi-dev gdbm openssl-dev yaml-dev procps duplicity ncftp make \
									bison readline-dev libxml2-dev git docker xfsprogs net-tools py-pip python-dev ansible gcc python-dev python3 \
									linux-headers musl-dev iproute2 htop strace sshpass \
									bash libstdc++ sudo \
									ruby ruby-json ruby-io-console ruby-irb ruby-rake ruby-bundler ruby-dev && \
						rm -rf /var/cache/apk/*

# Add repo for runit & mongodb
RUN echo "@edge http://dl-cdn.alpinelinux.org/alpine/edge/main" >> /etc/apk/repositories
RUN echo "@edge http://dl-cdn.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories
RUN echo "@edge http://dl-cdn.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories
RUN apk update && apk --no-cache add mongodb-tools@edge && \
						rm -rf /var/cache/apk/*

COPY . /opt/job-framework
COPY docker/data /srv
COPY docker/startup.sh /startup.sh
WORKDIR /opt/job-framework
RUN /opt/job-framework/gradlew assemble

RUN adduser -h /srv -s /bin/bash -D -u 1000 jobs
RUN chown -R jobs:jobs /srv

# Allow installation of ansible
RUN echo "jobs ALL=(root) NOPASSWD:/usr/bin/pip install ansible" >> /etc/sudoers

VOLUME ["/srv"]
CMD ["/startup.sh"]
