# Docker multi-stage build

# Building the App with Maven
FROM maven:3-jdk-11

ADD . /arete
WORKDIR /arete

# Just echo so we can see, if everything is there :)
RUN ls -l

# Run Maven build
RUN mvn clean install

# Just using the build artifact and then removing the build-container
FROM openjdk:11-jdk

MAINTAINER Agonaudid

VOLUME /tmp

# Add Spring Boot app.jar to Container
COPY --from=0 "/automated-testing-service/target/arete-0.0.1-SNAPSHOT.jar" arete.jar

ENV JAVA_OPTS=""

# Fire up our Spring Boot app by default
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /arete.jar" ]

EXPOSE 8098:8098
