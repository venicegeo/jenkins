#!groovy

// This is the seed job that creates all of the other jobs
// By adding to the venice.json, you can create new jobs
// and update parameters without needing to edit this file


String configfile = readFileFromWorkspace("venice.json")

def slurper = new groovy.json.JsonSlurper()
def config = slurper.parseText(configfile)

def baseFolderName = config.basefolder
folder("${baseFolderName}")

// Project Pipelines
for (project in config.projects) {
  folder("${baseFolderName}/${project.foldername}") {
    displayName("${project.foldername} pipelines")
  }
  folder("${baseFolderName}/${project.foldername}/${config.promotion.foldername}") {
    displayName("${project.foldername} promotion jobs")
  }
  for (repo in project.repos) {
    pipelineJob("${baseFolderName}/${project.foldername}/${repo.name}-pipeline") {
      description("${repo.name} pipeline")
      triggers {
        gitHubPushTrigger()
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

    if (repo.promotable) {
      pipelineJob("${baseFolderName}/${project.foldername}/${config.promotion.foldername}/${repo.name}-promote-pipeline") {
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
          for(param in config.promotion.jobparams) {
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
}
// Tools Pipelines
for (tool in config.tools) {
  folder("${baseFolderName}/${config.tools.foldername}") {
    displayName("venice tools pipelines")
  }
  for (repo in project.repos) {
    pipelineJob("${baseFolderName}/${config.tools.foldername}/${repo.name}-pipeline") {
      description("${repo.name} pipeline")
      triggers {
        gitHubPushTrigger()
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
  }
}