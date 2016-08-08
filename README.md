# Jenkins Build Pipelines

This is the repo for Venice's Jenkins Seed Job. [More Information](https://github.com/venicegeo/venice/blob/master/docs/devops.md#jenkins).

## What is a seed job?

The seed job is responsible for generating all other Jenkins jobs.

## How do I create a Jenkins job?

All jobs are spec'd out in `./Repos.groovy`. To add jobs for your repo, just add your jobs to the `repos` collection in `./Repos.groovy`:

```groovy
class Repos {
  static repos = [
  ...
    [
      reponame: 'your-venicegeo-repo',
      pipeline: ['job1','job2']
    ]
  ...
  ]
}
```

Then create `./ci/job1.sh` and `./ci/job2.sh` in `your-venicegeo-repo`.

And then, when Jenkins fails to automatically build your project for you, go to your project's "Webooks & Services" page on GitHub and add a new service of type "Jenkins (GitHub)". Set the URL to https://jenkins.devops.geointservices.io/github-webhook/.

## Special Sauce
* Versioning - the automation pipeline follows a versioning convention that is coupled with the git revision (i.e. we're not aiming for human readability).
* `./ci/vars.sh` - projects should provide the automation with a file `./ci/vars.sh` that defines two variables `APP` and `EXT`:

  ```
    APP=pz-app
    EXT=jar
  ```

* `archive` - archive jobs automagically push your artifact to nexus.
  - Required: `./ci/archive.sh` needs to build your artifact and move it to `./$APP.$EXT`.
* `cf_push_int` - push app to Cloud Foundry int environment.
* `cf_bg_deploy_int` - complete the blue/green deploy for an app.
* Static file projects - the automation will expect a `tar.gz` file to push to nexus; it is recommended to do something like: `./ci/archive.sh`:

  ```
    #!/bin/bash -ex

    pushd `dirname $0`/.. > /dev/null
    root=$(pwd -P)
    popd > /dev/null

    # APP=<my-project>
    # EXT=tar.gz
    source $root/ci/vars.sh
    tar -czf $APP.$EXT -C $root <directory-that-contains-the-static-files>
  ```

* Binary projects (I'm looking at you `golang`) - `EXT=bin` lets the automation know we're dealing with an executable.

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
