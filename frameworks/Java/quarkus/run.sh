#!/bin/bash

TAG=${1:-1.x}

docker stop tfb-database || echo "Database was not running"
docker run --rm -d --network=tfb --name tfb-database techempower/postgres
sleep 10

RESULTS=${2:-/tmp/results/$TAG/}
DOCKER_WRK=(docker run --rm -it --network=tfb --name wrk techempower/tfb.wrk wrk -H 'Host: tfb-server' -H 'Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7' -H 'Connection: keep-alive' --latency)
mkdir -p $RESULTS

for variant in $(ls -1 *.dockerfile | sed 's/\..*//'); do
  docker run --rm -d --network=tfb --name tfb-server techempower/tfb.test.$variant:$TAG
  sleep 5
  echo Running Primer plaintext
  "${DOCKER_WRK[@]}" -d 5 -c 8 --timeout 8 -t 8 http://tfb-server:8080/plaintext
  echo Running Warmup plaintext
  "${DOCKER_WRK[@]}" -d 15 -c 512 --timeout 8 -t 8 http://tfb-server:8080/plaintext
  echo Running the test
  for connections in 256 1024 4096 16384; do
    "${DOCKER_WRK[@]}" -d 15 -c $connections --timeout 8 -t 8 http://tfb-server:8080/plaintext -s pipeline.lua -- 16 | tee $RESULTS/$variant-$connections.txt
  done
  docker stop tfb-server
done

rm $RESULTS/results.txt 2> /dev/null
for result in $(ls -1 $RESULTS); do
  sed -n 's/Requests[^0-9]*/'${result%.*}' /p' $RESULTS/$result >> $RESULTS/results.txt
done
