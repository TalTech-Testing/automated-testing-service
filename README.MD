# Automated testing service

Documentation is available at [gitlab pages](https://ained.pages.taltech.ee/it-doc/arete/index.html#)

# Running locally

Prerequisites:

- Java 11+
- Docker
- next environment variables:
    - `ARETE_HOME` - Path where you cloned this project in file system. (Project should be refactored at some point not to need it)
    - `DOCKER_HOST` - Host, where docker is visible. If unix system, then this step can be skipped. On windows the value could be `tcp://localhost:2375`. Needed for Docker-Java
    - `GIT_PASSWORD` - password to authenticate against git so repositories can be cloned.
    - `GIT_USERNAME` - username to ...

Running tests:

if Windows
````cmd
mvnw.cmd test
````
else
````cmd
./mvnw test
````

Running the application:

if Windows
````cmd
mvnw.cmd package
java -jar target/automated_testing_service-2.0.jar
````
else
````cmd
./mvnw package
java -jar target/automated_testing_service-2.0.jar
````
