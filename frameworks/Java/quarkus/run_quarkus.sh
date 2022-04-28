#!/bin/bash

JAVA_OPTIONS="-server \
  -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:+DebugNonSafepoints \
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
