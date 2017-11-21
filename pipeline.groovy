#!groovy

// This is the seed job that creates all of the other jobs
// By adding to the venice.json, you can create new jobs
// and update parameters without needing to edit this file

String configfile = readFileFromWorkspace("venice.json")

def slurper = new groovy.json.JsonSlurper()
def veniceprojects = slurper.parseText(configfile)

for (project in veniceprojects.projects) {
  folder("venice/${project.foldername}") {
    displayName("${project.foldername} pipelines")
  }
  for (repo in project.repos) {
    pipelineJob("venice/${project.foldername}/${repo.name}-pipeline") {
      description("${repo.name} pipeline")
      triggers {
        gitlabPush()
      }
      environmentVariables {
      if (repo.threadfixId) {
        env('THREADFIX_ID', "${repo.threadfixId}")
      }
      }
      definition {
        cpsScm {
          scm {
          if (repo.scriptpath) {
            scriptPath("${repo.scriptpath}")
          }
            git {
              remote {
                url("${repo.url}")
                branch("*/UC-Stable")
                credentials("aaef610c-9fd0-4812-9027-755ff3a872a5")
              }
            }
          }
        }
      }
      parameters {
        for(param in project.jobparams) {
          if (param.type == "booleanParam") {
          "${param.type}"("${param.name}", "${param.defaultvalue}".toBoolean(), "${param.description}")
          } else {
          "${param.type}"("${param.name}", "${param.defaultvalue}", "${param.description}")
          }
        }
        stringParam("GIT_URL", "${repo.url}", "Git repository URL")
        for(credparam in project.credparams) {
          credentialsParam("${credparam.name}") {
            defaultValue("${credparam.defaultvalue}")
            description("${credparam.description}")
          }
        }
      }
    }
  }
}
