#!/bin/bash

# JFR: -XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=/quarkus/trace.jfr
#       and use docker cp to read it

# PROFILING: -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints

# DEBUG: -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005

# Consider -Dquarkus.http.io-threads==$((`grep --count ^processor /proc/cpuinfo`)) \

JAVA_OPTIONS="-server \
  -Dio.netty.buffer.checkBounds=false \
  -Dio.netty.buffer.checkAccessible=false \
  -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  -XX:-UseBiasedLocking \
  -XX:+UseStringDeduplication \
  -XX:+UseNUMA \
  -XX:+UseParallelGC \
  -Djava.lang.Integer.IntegerCache.high=10000 \
  -Dvertx.disableHttpHeadersValidation=true \
  -Dvertx.disableMetrics=true \
  -Dvertx.disableH2c=true \
  -Dvertx.disableWebsockets=true \
  -Dvertx.flashPolicyHandler=false \
  -Dvertx.threadChecks=false \
  -Dvertx.disableContextTimings=true \
  -Dhibernate.allow_update_outside_transaction=true \
  -Djboss.threads.eqe.statistics=false \
  $@"

java $JAVA_OPTIONS -jar quarkus-run.jar
