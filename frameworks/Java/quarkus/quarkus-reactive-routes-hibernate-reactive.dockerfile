FROM maven:3.6.3-jdk-11-slim as maven
ARG QUARKUS_VERSION=999-SNAPSHOT
WORKDIR /quarkus
ENV MODULE=reactive-routes-hibernate-reactive

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
ENV MODULE=reactive-routes-hibernate-reactive

COPY --from=maven /quarkus/$MODULE/target/quarkus-app/lib/ lib
COPY --from=maven /quarkus/$MODULE/target/quarkus-app/app/ app
COPY --from=maven /quarkus/$MODULE/target/quarkus-app/quarkus/ quarkus
COPY --from=maven /quarkus/$MODULE/target/quarkus-app/quarkus-run.jar quarkus-run.jar

EXPOSE 8080

CMD ["java", "-server", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-XX:-UseBiasedLocking", "-XX:+UseStringDeduplication", "-XX:+UseNUMA", "-XX:+UseParallelGC", "-Djava.lang.Integer.IntegerCache.high=10000", "-Dvertx.disableHttpHeadersValidation=true", "-Dvertx.disableMetrics=true", "-Dvertx.disableH2c=true", "-Dvertx.disableWebsockets=true", "-Dvertx.flashPolicyHandler=false", "-Dvertx.threadChecks=false", "-Dvertx.disableContextTimings=true", "-Dhibernate.allow_update_outside_transaction=true", "-Djboss.threads.eqe.statistics=false", "-jar", "quarkus-run.jar"]
