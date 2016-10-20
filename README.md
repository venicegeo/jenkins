# Jenkins Build Pipelines

This is the repo for Venice's Jenkins Seed Job. [More Information](https://github.com/venicegeo/venice/blob/master/docs/devops.md#jenkins).

## What is a seed job?

The seed job is responsible for generating all other Jenkins jobs.

## How do I create a Jenkins job?

All jobs are spec'd out in `./venice-pipeline.groovy`. To add jobs for your repo, just add your jobs to the `projects` collection in `./venice-pipelin.groovy`:

```groovy
def projects = ['ingest', 'idam', 'gateway', 'yourproject']
```

And then, when Jenkins fails to automatically build your project for you, go to your project's "Webooks & Services" page on GitHub and add a new service of type "Jenkins (GitHub)". Set the URL to https://jenkins.devops.geointservices.io/github-webhook/.

The pipeline implementation relies on a `JenkinsFile` located within the code repository. After adding your project,
ensure you have a `JenkinsFile` in your repository. You can find out more about [using a `JenkinsFile`](https://jenkins.io/doc/book/pipeline/jenkinsfile/) in the introduction.

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
