# job-framework

This is a basic Job Management Framework.

## Test

You can simply test it with docker-compose.
If you have installed it just execute the following steps:
 
 ```
 git clone https://github.com/Odania-IT/job-framework.git
 cd job-framework
 docker-compose up
 ```
 
 Afterwords you can go to:
 - http://www.lvh.me:8000 <- Application
 - http://www.lvh.me:1080 <- Dummy Mail Client
 
 If you want to test your own pipeline just mount a volume to /srv.
 Example content can be found here: 
 https://github.com/Odania-IT/job-framework/tree/master/docker

## Production

A complete build is available directly via https://hub.docker.com/r/odaniait/job-framework

If you want to run it simply use docker:

```
docker run --name jobframework -p 8000:8000 -v /media/job-framework-data:/srv odaniait/job-framework
```

In this example the folder "/media/job-framework-data" contains
the data from 

### Docker support in container

If you need docker inside the container you can bind the
docker socket to the container

```
-v /var/run/docker.sock:/var/run/docker.sock
```

Now you can use the docker of the host inside the container.

## Pipelines

Everything that can be defined is a pipeline. A pipeline consists of multiple steps.
Steps can be executed in sequence, parallel and on trigger.

A pipeline can be started manually or via cron. For cleanup you can define the number of builds to keep.
You can allow multiple simultanious exections or only one at a time.

### Steps

Steps contain multiple jobs. The jobs are executed in sequence. If a job fails the execution is stopped.

### Jobs

A Job consists a shell script that should be executed.

## Views

Views can show filtered pipelines. You can define pipelines manually or use a pattern.

## Configuration

The folders, mail settings, etc are done with the spring configuration. 

jobframework.baseDirectory <= The base work directory.
jobframework.from_mail <= Notification Mail sender (Only required if Mail Notification is used)

// Spring Mail Settings
spring.mail.host=localhost
spring.mail.password=
spring.mail.port=2525
spring.mail.protocol=smtp
spring.mail.test-connection=false
spring.mail.username=

### Directory Structure

pipelines
  - pipeline-1
    - settings.yml
    - state.yml
    - builds
      - 00000001
        - info.yml
      - 00000002
        - info.yml
  - ...
workspace
  - pipeline-1
    - 00000001
      - Step 1
        - Build Content for the Step
      - ...
    - ...

### Views

The configuration is done in YAML files. The views are configured in the settings.yml in the work folder.
A view can filter on tags, a pattern or list specified pipelines.

### Pipelines

Pipelines are defined in the pipelines folder. The folder is used as the name of the pipeline.
The pipeline configuration is defined in the config.yml folder.

# Todo / Ideas

- Execution limit for jobs or steps
- Reload config with or without waiting for finished jobs
- Error View (Special Logging)

# FAQ

## Why not Jenkins?

Jenkins is a good tool with hundreds of plugins. Sometimes i had problems that Jenkins is not working as expected.

E.g. it is sometimes a bit messy to get the working plugin versions so that everything runs as expected.

It also lacks some features in Regards of parameters.
