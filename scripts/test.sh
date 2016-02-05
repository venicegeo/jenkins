#!/bin/bash

pushd `dirname $0`/.. > /dev/null
base=$(pwd -P)

jarfile=$base/scripts/job-dsl-core-1.43-SNAPSHOT-standalone.jar

java -jar $jarfile seed.groovy

ls -1 *.xml | sed 's/^/generated: /'

rm -f *.xml

popd > /dev/null
