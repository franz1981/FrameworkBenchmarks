FROM maven:3.6.3-jdk-11-slim as maven
ARG QUARKUS_VERSION=999-SNAPSHOT
WORKDIR /quarkus
ENV MODULE=resteasy-reactive-pgclient

COPY pom.xml pom.xml
COPY $MODULE/pom.xml $MODULE/pom.xml

# Uncomment to test pre-release quarkus
RUN mkdir -p /root/.m2/repository/io
COPY m2-quarkus /root/.m2/repository/io/quarkus

WORKDIR /quarkus/$MODULE
RUN mvn dependency:go-offline -q -Dquarkus.version=$QUARKUS_VERSION
WORKDIR /quarkus

COPY $MODULE/src $MODULE/src

WORKDIR /quarkus/$MODULE
RUN mvn package -q -Dquarkus.version=$QUARKUS_VERSION
WORKDIR /quarkus

FROM openjdk:11.0.6-jdk-slim
WORKDIR /quarkus
ENV MODULE=resteasy-reactive-pgclient

COPY --from=maven /quarkus/$MODULE/target/quarkus-app/lib/ lib
COPY --from=maven /quarkus/$MODULE/target/quarkus-app/app/ app
COPY --from=maven /quarkus/$MODULE/target/quarkus-app/quarkus/ quarkus
COPY --from=maven /quarkus/$MODULE/target/quarkus-app/quarkus-run.jar quarkus-run.jar
ADD run_quarkus.sh /quarkus/run_quarkus.sh
RUN chmod a+x /quarkus/run_quarkus.sh

EXPOSE 8080
ENTRYPOINT "./run_quarkus.sh"
