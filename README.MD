
# Currently supported endpoints

Schemas for post are located at ```/arete/schemas```

* post
    * /test
    * /test/sync <WIP>
    * /tests/update/{project_base}/{project}
    * /image/update/java-tester

* get
    * /logs
    * /logs/{hash}

Getting Started
----

### Manual setup

##### Local docker with postgres ####

You need latest of: Docker

```
git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service.git
cd automated-testing-service
docker-compose up -d
```

##### Local host machine with H2 ####
You need latest of: Java11, Docker
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
___
##### Shell config for dind

___
/etc/gitlab-runner/config.toml
```
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
```
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
```
./arete_local_install.sh
```

Scripts have been tested on Ubuntu 18.04 and 19.04
