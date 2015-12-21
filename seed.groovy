def projects = [
  [name: 'pz-dispatcher', jobs: ['setup', 'build']],
  [name: 'pz-gateway', jobs: ['setup', 'build']],
  [name: 'pz-jobcommon', jobs: ['setup', 'build']],
  [name: 'pz-jobmanager', jobs: ['setup', 'build']],
  [name: 'pz-serviceregistry', jobs: ['setup', 'build']]
]

for (project in projects) {
  for (job in project.jobs) {

    freeStyleJob ("${project.name}-${job}") {
      properties {
        githubProjectUrl "https://github.com/venicegeo/${project.name}"
      }

      scm {
        git {
          remote {
            github "venicegeo/${project.name}"
            credentials 'fa3aab48-4edc-446d-b1e2-1d89d86f4458'
          }
        }
      }

      triggers {
        githubPush()
      }

      logRotator { numToKeep 30 }

      steps {
        shell("./${job}.sh")
      }

      wrappers {
        colorizeOutput()
      }
    }

  }
}
