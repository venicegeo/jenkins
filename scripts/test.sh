#!/bin/bash

pushd `dirname $0`/.. > /dev/null
base=$(pwd -P)

jarfile=$base/scripts/job-dsl-core-1.46-SNAPSHOT-standalone.jar

java -jar $jarfile run.groovy >/dev/null

popd > /dev/null
