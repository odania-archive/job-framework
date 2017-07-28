FROM openjdk:8-jdk-alpine
MAINTAINER Mike Petersen <mike@odania-it.de>

RUN apk update && apk --no-cache add vim curl autoconf zlib-dev unzip bzip2 ca-certificates libffi-dev gdbm  \
									bison readline-dev libxml2-dev git docker xfsprogs net-tools py-pip python-dev ansible gcc python-dev python3 \
									linux-headers musl-dev iproute2 htop strace sshpass openssh-client \
									bash libstdc++ sudo openssl-dev yaml-dev procps duplicity ncftp make \
									ruby ruby-json ruby-io-console ruby-irb ruby-rake ruby-bundler ruby-dev go \
									&& rm -rf /var/cache/apk/*

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

RUN mkdir -p /home/jobs
RUN adduser -h /home/jobs -s /bin/bash -D -u 1000 jobs
RUN chown -R jobs:jobs /srv
RUN chown -R jobs:jobs /home/jobs
RUN chown -R jobs:jobs /opt/job-framework

COPY docker/build.sh /build.sh
RUN /build.sh

# AWS ECR Login Helper for docker
RUN mkdir -p /usr/lib/go/src/github.com/awslabs && \
	git clone https://github.com/awslabs/amazon-ecr-credential-helper.git /usr/lib/go/src/github.com/awslabs/amazon-ecr-credential-helper && \
	cd /usr/lib/go/src/github.com/awslabs/amazon-ecr-credential-helper && \
	make && mv bin/local/docker-credential-ecr-login /usr/local/bin/docker-credential-ecr-login
ADD docker/docker-config.json /srv/.docker/config.json


# Allow installation of ansible
RUN echo "jobs ALL=(root) NOPASSWD:/usr/bin/pip install ansible" >> /etc/sudoers

VOLUME ["/srv"]
CMD ["/startup.sh"]
USER jobs
