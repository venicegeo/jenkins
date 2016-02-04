class PipelineJob {
  def project
  def step
  def job
  def branch
  def cfapi
  def cfdomain

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
          branch("${this.branch}")
        }
      }

      steps {
        shell("""
          git clean -xffd
          [ -f ./scripts/${this.step}.sh ] || { echo "noop"; exit; }
          chmod 700 ./scripts/${this.step}.sh
          ./scripts/${this.step}.sh
          exit \$?
        """)
      }

      logRotator { numToKeep 30 }

      publishers {
        slackNotifications {
          notifyFailure()
          notifyNotBuilt()
          notifyUnstable()
          notifyBackToNormal()
        }
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

  def deploy() {
    this.job.with {
      steps {
        shell("""
          current=`cf routes | grep ${this.project} | awk '{print \$4}'`
          cf map-route ${this.project}-\$GIT_REVISION ${this.cfdomain} -n ${this.project}
          cf delete -f \$current
        """)
      }
    }
  }

  def triggerTeardown() {
    this.job.with {
      publishers {
        downstream("${this.project}-cf-teardown", "UNSTABLE")
        downstream("${this.project}-cf-teardown", "FAILURE")
      }
    }
  }

  def teardown() {
    this.job.with {
      steps {
        shell("cf delete -f ${this.project}-\$GIT_REVISION")
      }
    }
  }

  def deliver() {
    this.job.with {
      configure { project ->
        project / publishers << 'com.hpe.cloudfoundryjenkins.CloudFoundryPushPublisher' {
          target "${this.cfapi}"
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

