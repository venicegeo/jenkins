package lib

class Base {
  def jobject
  def config
  def slack_message

  def defaults() {
    def release = (this.config.gh_repo == 'pz-release') && (this.config.gh_branch != 'ci')
    this.jobject.with {
      wrappers { 
        colorizeOutput()
      }

      logRotator { numToKeep 30 }

      publishers {
        slackNotifications {
          projectChannel release ? '#release' : '#jenkins'
          integrationToken this.config.slack_token
          configure { node ->
            teamDomain this.config.slack_domain
            startNotification false
            notifySuccess release ? true : false
            notifyAborted release ? false : true
            notifyNotBuilt release ? false : true
            notifyUnstable release ? false : true
            notifyFailure true
            notifyBackToNormal release ? false : true
            notifyRepeatedFailure release ? false : true
            includeTestSummary false
            showCommitList release ? true : false
            includeCustomMessage true
            customMessage this.slack_message || this.config.slack_message
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
      }

      scm {
        git {
          remote {
            github("${this.config.gh_org}/${this.config.gh_repo}", 'ssh')
            credentials "95eee62c-dc20-44d5-a141-14a11856421e"
          }
          localBranch("${this.config.gh_branch}")
          branch("${this.config.gh_branch}")
        }
      }

    }

    return this
  }
}
