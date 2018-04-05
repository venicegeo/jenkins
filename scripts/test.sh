#!/bin/bash

pushd `dirname $0`/.. > /dev/null
base=$(pwd -P)

jarfile=$base/scripts/job-dsl-core-1.50-standalone.jar

java -jar $jarfile seed.groovy >/dev/null

popd > /dev/null
