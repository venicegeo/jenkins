package lib

class Base {
  def jobject
  def config
  def slack_message
  def promotion

  def defaults() {
    this.jobject.with {
      configure { project ->
        project << assignedNode('sl61')
        project << canRoam('false')
      }

      wrappers { 
        colorizeOutput()
        golang('golang_1.7')
      }

      logRotator { numToKeep 30 }

      publishers {

        wsCleanup {
          excludePattern 'pipeline.properties'
        }

        slackNotifications {
          projectChannel this.promotion ? '#release' : '#jenkins'
          integrationToken this.config.slack_token
          configure { node ->
            notifySuccess this.promotion ? true : false
            notifyAborted this.promotion ? false : true
            notifyNotBuilt this.promotion ? false : true
            notifyUnstable this.promotion ? false : true
            notifyFailure true
            notifyBackToNormal this.promotion ? false : true
            notifyRepeatedFailure this.promotion ? false : true
            teamDomain this.config.slack_domain
            startNotification false
            includeTestSummary false
            includeCustomMessage true
            customMessage this.slack_message ? this.slack_message : this.config.slack_message
          }
        }
      }

    }

    return this
  }

  def parameters() {
    this.jobject.with {

      parameters {
        choiceParam('target_domain', this.config.domains, this.config.domains_description )
        stringParam('component_revision', 'latest', 'commit sha, git branch or tag to build (default: latest component_revision)')
      }

    }

    return this
  }

  def pipeline_parameters() {
    this.jobject.with {

      parameters {
        stringParam('component', "${this.config.gh_repo}", 'the component this pipeline builds')
        stringParam('component_revision', 'latest', 'commit sha, git branch or tag to build (default: latest component_revision)')
      }

    }

    return this
  }

  def github() {
    this.jobject.with {

      properties {
        githubProjectUrl "https://github.com/${this.config.gh_org}/${this.config.gh_repo}"
        gitLabConnectionProperty {
          gitLabConnection "GitLab.d.gs.io"
        }
      }

      scm {
        git {
          remote {
            github("${this.config.gh_org}/${this.config.gh_repo}", 'ssh')
            credentials "95eee62c-dc20-44d5-a141-14a11856421e"
          }
          branch("${this.config.gh_branch}")
        }
      }

    }

    return this
  }

  def selenium() {
    this.jobject.with {
      wrappers {
        xvfb('Default') {
          screen('1920x1080x24')
          parallelBuild(true)
        }
      }
    }

    return this
  }
}
