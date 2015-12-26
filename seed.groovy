def projects = [
  [name: 'refapp-devops', jobs: ['setup', 'artifact', 'build-test', 'deliver']]
]

for (p in projects) {
  for (job in p.jobs) {

    freeStyleJob ("${p.name}-${job}") {
      properties {
        githubProjectUrl "https://github.com/venicegeo/${p.name}"
      }

      scm {
        git {
          remote {
            github "venicegeo/${p.name}"
            credentials 'fa3aab48-4edc-446d-b1e2-1d89d86f4458'
          }
        }
      }

      triggers {
        if (job != 'deliver') {
          githubPush()
        }
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

      if (job == 'deliver') {
        configure { project ->
          project / publishers << 'com.hpe.cloudfoundryjenkins.CloudFoundryPushPublisher' {
            target 'http://api.cf.piazzageo.io'
            organization 'piazza'
            cloudSpace 'dev'
            credentialsId 'ff5565ae-2494-45c0-ac9a-d01003a34096'
            selfSigned true
            resetIfExists false
            pluginTimeout 120
            servicesToCreate
            manifestChoice {
              value 'manifestFile'
              manifestFile 'manifest.yml'
              memory 0
              instances 0
              noRoute false
            }
          }
        }
      }
    }

  }
}
