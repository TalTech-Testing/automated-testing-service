# Docker multi-stage build
# Just using the build artifact and then removing the build-container
FROM ubuntu:19.04

MAINTAINER Agonaudid
ADD . /arete
WORKDIR /arete

ARG PASSWORD
ENV AUTOMATED_TESTING_SERVER_SECRET_PASSWORD=${PASSWORD}


# Just echo so we can see, if everything is there :)
RUN ls -l

RUN apt-get update

# install wget
RUN apt-get install -y wget

# install apt-utils
RUN apt-get install -y apt-utils

# get docker
RUN apt-get install -y apt-transport-https ca-certificates curl gnupg-agent software-properties-common
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable edge"

RUN apt-get update
RUN apt-get install -y docker-ce
RUN systemctl status docker

# get maven 3.6.2
RUN wget --no-verbose -O /tmp/apache-maven-3.6.2.tar.gz http://archive.apache.org/dist/maven/maven-3/3.6.2/binaries/apache-maven-3.6.2-bin.tar.gz

# install maven
RUN tar xzf /tmp/apache-maven-3.6.2.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.6.2 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.6.2.tar.gz
ENV MAVEN_HOME /opt/maven

# remove download archive files
RUN apt-get clean

VOLUME /tmp

RUN apt-get update && apt-get install -y locales && rm -rf /var/lib/apt/lists/* \
    && localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8


# Install OpenJDK-13
RUN apt-get update && \
    apt-get install -y openjdk-13-jdk && \
    apt-get install -y ant && \
    apt-get clean;

RUN apt-get install -y default-jre

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-13-openjdk-amd64/
RUN export JAVA_HOME


ENV LANG en_US.utf8
ENV JAVA_OPTS=""

# Run Maven build
RUN mvn clean install

# Add Spring Boot app.jar to Container
COPY "/arete/target/arete-0.0.1-SNAPSHOT.jar" arete.jar

# Fire up our Spring Boot app by default
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /arete.jar" ]

EXPOSE 8098:8098
