#!/bin/bash

if [ $1 == "1" ]; then
  QUARKUS_VERSION=1.13.1.Final
  TAG=1.x
elif [ $1 == "2" ]; then
  QUARKUS_VERSION=999-SNAPSHOT
  TAG=2.x
else
  echo "Set version 1 or 2"
  exit 1
fi
echo Building images for Quarkus $QUARKUS_VERSION
shift

if [ -z $1 ]; then
   VARIANTS=$(ls *.dockerfile)2021
else
   VARIANTS=$@
fi
echo Variants: $VARIANTS

for variant in $VARIANTS; do
  docker build -t techempower/tfb.test.${variant%.*}:$TAG -f ${variant} --build-arg QUARKUS_VERSION=$QUARKUS_VERSION .
done
