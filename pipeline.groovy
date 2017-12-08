#!groovy

// This is the seed job that creates all of the other jobs
// By adding to the venice.json, you can create new jobs
// and update parameters without needing to edit this file


String configfile = readFileFromWorkspace("venice.json")

def slurper = new groovy.json.JsonSlurper()
def config = slurper.parseText(configfile)

def baseFolderName = config.basefolder
folder("${baseFolderName}")

for (project in config.projects) {
  folder("${baseFolderName}/${project.foldername}") {
    displayName("${project.foldername} pipelines")
  }
  for (repo in project.repos) {
    pipelineJob("${baseFolderName}/${project.foldername}/${repo.name}-pipeline") {
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
            scriptPath("JenkinsFile")
            git {
              remote {
                url("${repo.url}")
                branch("*/master")
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

    //promotion jobs
    pipelineJob("${baseFolderName}/${project.foldername}/${repo.name}-promote-pipeline") {
      description("${repo.name} promotion pipeline")
      definition {
        cpsScm {
          scm {
            scriptPath("JenkinsFile.Promote")
            git {
              remote {
                url("${repo.url}")
                branch("*/master")
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