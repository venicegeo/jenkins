def projects = [
  [name: 'refapp-devops', jobs: ['setup']]
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
        shell("git clean -xffd")
        shell("./build/${job}.sh")
      }

      publishers {
        slackNotifications {
          notifyBuildStart()
          notifyAborted()
          notifyFailure()
          notifyNotBuilt()
          notifySuccess()
          notifyUnstable()
          notifyBackToNormal()
        }
      }

      wrappers {
        colorizeOutput()
      }
    }

  }
}
