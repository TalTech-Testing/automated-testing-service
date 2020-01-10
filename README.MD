
# Currently supported endpoints

Schemas can be generated from arete/api/data ```/request``` and ```/response```

* post
    * /test
    * /test/sync
    * /tests/update
    * /image/update/{image_name}
        * java-tester
        * python-tester

* get
    * /submissions
    * /submissions/{hash}
    * /submissions/{hash}/logs
    * /submissions/active
    * /debug/{1 or 0}
    * /logs

Getting Started
----

### Manual setup

##### Local docker with postgres ####

You need latest of: Docker

```shell script
git clone https://github.com/envomp/arete
docker login -u automatedtestingservice -p $DOCKER_PASSWORD
docker-compose up -d
```

##### Local host machine with H2 ####
You need latest of: Java11, Docker, System Environments for ARETE_HOME, GITLAB_PASSWORD
___
##### for Linux
```shell script
git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service
./mvnw clean install
java -jar -Dspring.config.location=src/main/resources/application-dev.properties target/arete-0.0.1-SNAPSHOT.jar
```

##### for Windows

```shell script
git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service
mvnw.cmd clean install
java -jar -Dspring.config.location=src/main/resources/application-dev.properties target/arete-0.0.1-SNAPSHOT.jar
```

#### Server
___
##### Shell config for dind

___
/etc/gitlab-runner/config.toml
```shell script
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "Shell"
  url = "https://gitlab.cs.ttu.ee/"
  token = "XXX"
  executor = "shell"
  [runners.custom_build_dir]
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
```
___
##### Virtualbox config

___
C:/gitlab-runner/config.toml
```shell script
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "VM-runner"
  url = "https://gitlab.cs.ttu.ee/"
  token = "XXX"
  executor = "virtualbox"
  [runners.custom_build_dir]
  [runners.ssh]
    host = "192.168.56.101"
    user = "envomp"
    password = "XXX"
    port = "22"
  [runners.virtualbox]
    base_name = "Server"
    disable_snapshots = false
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
```

run in terminal:
```shell script
./arete_local_install.sh
```

cleanup:
```shell script
./docker_cleanup.sh
```

Scripts have been tested on Ubuntu 18.04 and 19.04
