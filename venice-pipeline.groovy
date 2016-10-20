#!groovy
// This is the initial seed job for venicegeo which creates all of ther
// other pipeline jobs. Each repo must contain a JenkinsFile
// that denotes the steps to take when building.

def gitprefix = 'https://github.com/venicegeo/pz-'
def projects = ['ingest', 'idam', 'gateway']
for(i in projects) {
  pipelineJob("shiro-4-${i}") {
  triggers {
    gitHubPushTrigger()
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            url("${gitprefix}${i}")
            }
          }
        }
      }
    }
  }
}

