#!/bin/bash

WRK_PATH=/home/forked_franz/IdeaProjects/wrk

URL=plaintext

DURATION=20

EVENT=cpu

# this can be html or jfr
FORMAT=html

PIPELINE=1

THREADS=1

while getopts ":u::e::f::d::p::t:" option; do
   case $option in
      u) URL=${OPTARG}
         ;;
      e) EVENT=${OPTARG}
         ;;
      f) FORMAT=${OPTARG}
         ;;
      d) DURATION=${OPTARG}
         ;;
      p) PIPELINE=${OPTARG}
         ;;
      t) THREADS=${OPTARG}
         ;;
   esac
done

WARMUP=$((${DURATION}*2/5))

PROFILING=$((${DURATION}/2))

FULL_URL=http://localhost:8080/${URL}

echo "Benchmarking endpoint ${FULL_URL}..."

# set sysctl kernel variables only if necessary
current_value=$(sysctl -n kernel.perf_event_paranoid)
if [ "$current_value" -ne 1 ]; then
  sudo sysctl kernel.perf_event_paranoid=1
  sudo sysctl kernel.kptr_restrict=0
fi

trap 'echo "cleaning up server process";kill ${server_pid}' SIGINT SIGTERM SIGKILL

# add to check ASM of some compiled method -XX:+UnlockDiagnosticVMOptions '-XX:CompileCommand=print,*Parser.*'

# -Dio.netty.buffer.checkBounds=false -Dio.netty.buffer.checkAccessible=false

java -Dio.netty.buffer.checkBounds=true -Dio.netty.buffer.checkAccessible=true -Dio.netty.eventLoopThreads=${THREADS} -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+UseParallelGC -jar ../target/netty-example-0.1-jar-with-dependencies.jar &
server_pid=$!

sleep 2

echo "Server running at pid $server_pid with $THREADS threads"

echo "Warming-up endpoint"

# warm it up, it's fine if it's blocking and max speed

${WRK_PATH}/wrk -H 'Host: 192.168.0.106' -H 'Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7' -H 'Connection: keep-alive' -c 10 -t 1 -d ${DURATION} $FULL_URL -s pipeline.lua -- ${PIPELINE}

echo "Warmup completed: start test and profiling"

${WRK_PATH}/wrk -H 'Host: 192.168.0.106' -H 'Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7' -H 'Connection: keep-alive' -c 10 -t 1 -d ${DURATION} $FULL_URL -s pipeline.lua -- ${PIPELINE} &

wrk_pid=$!

echo "Waiting $WARMUP seconds before profiling for $PROFILING seconds"

sleep $WARMUP

perf stat -p $server_pid &

java -jar ap-loader-all.jar profiler -e ${EVENT} -t -d ${PROFILING} -f ${server_pid}_${EVENT}.${FORMAT} $server_pid &

ap_pid=$!

echo "Showing pidstat for $WARMUP seconds"

pidstat -p $server_pid 1 &

pidstat_pid=$!

sleep $WARMUP

kill -SIGTERM $pidstat_pid

wait $wrk_pid

echo "Profiling and workload completed: killing server"

kill -SIGTERM $server_pid


