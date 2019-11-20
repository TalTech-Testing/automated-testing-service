# Database layout

TODO

# Currently supported endpoints

# Currently supported endpoints

Schemas for post are located at ```/arete/schema```

* post
    * /test

Getting Started
----

## Quick install and setup
##### Debian
run in terminal:
```
./arete_local_install.sh
```
This script has been tested on Ubuntu 18.04 and 19.04


## Manual setup

#### Local docker with postgres ####

You need latest of: Docker

```
git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service.git
cd automated-testing-service
docker-compose up -d
```

#### Local host machine with H2 ####
You need latest of: Java13, Docker

___
##### for Linux
```
./mvnw clean install
java -jar -Dspring.config.location=src/main/resources/application-dev.properties target/arete-0.0.1-SNAPSHOT.jar
```


##### for Windows

```
mvnw.cmd clean install
java -jar -Dspring.config.location=src/main/resources/application-dev.properties target/arete-0.0.1-SNAPSHOT.jar
```

#### Server

Docker config

___
/etc/gitlab-runner/config.toml
```
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "XXX"
  url = "XXX"
  token = "XXX"
  executor = "shell"
  [runners.custom_build_dir]
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
```