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
    return this.job.with {
      triggers {
        githubPush()
      }
    }
  }

  def deploy() {
    return this.job.with {
      steps {
        shell("""
          legacy=`cf routes | grep '${this.project} ' | awk '{print \$4}'`
          target=${this.project}-`git rev-parse HEAD`
          [ "\$target" = "\$legacy" ] && { echo "nothing to do."; exit 0; }
          cf map-route ${this.project}-`git rev-parse HEAD` ${this.cfdomain} -n ${this.project}
          s=\$?
          [ -n "\$legacy" ] && cf delete -f \$legacy || exit \$s
        """)
      }
    }
  }

  def triggerTeardown() {
    return this.job.with {
      publishers {
        flexiblePublish {
          conditionalAction {
            condition {
              status('ABORTED', 'FAILURE')
            }
            publishers {
              downstream("${this.project}-cf-teardown", "FAILURE")
            }
          }
        }
      }
    }
  }

  def teardown() {
    return this.job.with {
      steps {
        shell("cf delete -f ${this.project}-`git rev-parse HEAD`")
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

