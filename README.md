# Jenkins Seed

This is the repo for Venice's [Jenkins Seed Job](http://jenkins.piazzageo.io/job/seed).

## What is a seed job?

The seed job is responsible for generating all other Jenkins jobs.

## How do I create a Jenkins job?

All jobs are spec'd out in `./Projects.groovy`. To add jobs for your repo, just add your jobs to the `list` collection in `./Projects.groovy`:

```groovy
class Projects {
  static list = [
  ...
    [
      name: 'your-venicegeo-repo',
      pipeline: ['job1','job2']
    ]
  ...
  ]
}
```

Then create `./scripts/job1.sh` and `./scripts/job2.sh` in `your-venicegeo-repo`.

## How do I make Jenkins deploy my app to CloudFoundry?

- create a job `cf-deliver` and a `./manifest.yml` in your repo ([more on manifests](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html)).

## How do I trigger a Jenkins job?
- Right now the only way jobs are triggered is via a push to github.

## Testing seed job generation
```
./scripts/test.sh
```
