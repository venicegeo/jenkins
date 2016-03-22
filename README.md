# Jenkins Build Pipelines

This is the repo for Venice's Jenkins Seed Job.

## What is a seed job?

The seed job is responsible for generating all other Jenkins jobs.

## How do I create a Jenkins job?

All jobs are spec'd out in `./Projects.groovy`. To add jobs for your repo, just add your jobs to the `list` collection in `./Projects.groovy`:

```groovy
class Projects {
  static projects = [
  ...
    [
      name: 'your-venicegeo-repo',
      pipeline: ['job1','job2']
    ]
  ...
  ]
}
```

Then create `./ci/job1.sh` and `./ci/job2.sh` in `your-venicegeo-repo`.

## How do I make Jenkins deploy my app to CloudFoundry?

- add `stage` to your pipeline and a `./manifest.jenkins.yml` in your repo ([more on manifests](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html)).
- **Note:** you'll need an artifact in nexus!

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
