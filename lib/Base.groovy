package lib

class Base {
  def jobject
  def config

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
            customMessage "      revision: `\$revision`\n      domain: `\$target_domain`\n      commit sha: `\$GIT_COMMIT`"
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
        stringParam('revision', 'latest', 'commit sha, git branch or tag to build (default: latest revision)')
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
          }
          branch("${this.config.gh_branch}")
        }
      }

    }

    return this
  }
}
