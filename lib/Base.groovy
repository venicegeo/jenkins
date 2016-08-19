package lib

class Base {
  def jobject
  def config
  def slack_message

  def defaults() {
    this.jobject.with {
      wrappers { 
        colorizeOutput()
      }

      logRotator { numToKeep 30 }

      publishers {
        slackNotifications {
          projectChannel "#jenkins"
          integrationToken this.config.slack_token
          configure { node ->
            teamDomain this.config.slack_domain
            startNotification false
            notifySuccess false
            notifyAborted true
            notifyNotBuilt true
            notifyUnstable true
            notifyFailure true
            notifyBackToNormal true
            notifyRepeatedFailure true
            includeTestSummary false
            showCommitList false
            includeCustomMessage true
            customMessage this.slack_message
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

  def github() {
    this.jobject.with {

      properties {
        githubProjectUrl "https://github.com/${this.config.gh_org}/${this.config.gh_repo}"
      }

      scm {
        git {
          remote {
            github "${this.config.gh_org}/${this.config.gh_repo}"
            credentials "95eee62c-dc20-44d5-a141-14a11856421e"
          }
          branch("${this.config.gh_branch}")
        }
      }

    }

    return this
  }
}
