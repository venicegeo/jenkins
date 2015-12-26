class PJob {
  def project
  def step
  def job

  def base() {
    this.job.with {
      properties {
        githubProjectUrl "https://github.com/venicegeo/${this.project}"
      }

      scm {
        git {
          remote {
            github "venicegeo/${this.project}"
            credentials 'fa3aab48-4edc-446d-b1e2-1d89d86f4458'
          }
        }
      }

      steps {
        shell("git clean -xffd")
        shell("[ -x ./build/${this.step}.sh ] && ./build/${this.step}.sh || echo noop")
      }

      logRotator { numToKeep 30 }

      publishers {
        slackNotifications {
          notifyFailure()
          notifyNotBuilt()
          notifyUnstable()
          notifyBackToNormal()
        }
        downstream("${this.project}-teardown", "FAILURE")
        downstream("${this.project}-teardown", "UNSTABLE")
      }

      wrappers {
        colorizeOutput()
      }
    }

    return this
  }

  def trigger() {
    this.job.with {
      triggers {
        githubPush()
      }
    }

    return this
  }

  def deliver() {
    this.job.with {
      configure { project ->
        project / publishers << 'com.hpe.cloudfoundryjenkins.CloudFoundryPushPublisher' {
          target 'http://api.cf.piazzageo.io'
          organization 'piazza'
          cloudSpace 'dev'
          credentialsId 'ff5565ae-2494-45c0-ac9a-d01003a34096'
          selfSigned true
          resetIfExists false
          pluginTimeout 120
          servicesToCreate ''
          appURIs ''
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

    return this
  }
}

def projects = [
  [
    name: 'refapp-devops',
    pipeline: ['setup','build','build-test','artifact','artifact-test','deliver','teardown']
  ]
]


for (p in projects) {
  def jobs = [:]
  p.pipeline.eachWithIndex { s, i ->
    jobs[s] = new PJob([
      project: p.name,
      step: s,
      job: job("${p.name}-${s}")
    ])

    jobs[s].base()

    if (i == 0) {
      jobs[s].trigger()
    }

    if (s == 'deliver') {
      jobs[s].deliver()
    } else {
      jobs[s].job.with {
        publishers {
          downstream("${p.name}-${p.pipeline[i+1]}", "SUCCESS")
        }
      }
    }
  }

  deliveryPipelineView(p.name) {
    allowPipelineStart(true)
    allowRebuild(true)
    pipelineInstances(5)
    columns(5)
    updateInterval(60)
    pipelines {
      component(p.name, "${p.name}-${p.pipeline[0]}")
    }
  }
}
