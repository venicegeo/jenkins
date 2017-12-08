# Jenkins Build Pipelines

This is the repo for Venice's Jenkins Seed Job. [More Information](https://github.com/venicegeo/venice/blob/master/docs/devops.md#jenkins).

## What is a seed job?

The seed job is responsible for generating all other Jenkins jobs.

## Configuration File

The seed job points to the configuration file (`venice.json`) in order to pull in parameterized build parameters into project repositories. The JSON file consists of a group of project folders that define all of this information on a per-project basis. 

## Testing seed job generation
```
./scripts/test.sh
```

## License

Copyright 2016, RadiantBlue Technologies, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
