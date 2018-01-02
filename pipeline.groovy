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
  folder("${baseFolderName}/${project.foldername}/${config.nightly.foldername}") {
    displayName("${project.foldername} nightly jobs")
  }
  def promoteJobs = [] // Collect Promotable Jobs for a Master Promote Job
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
      def promoteJobName = "${baseFolderName}/${project.foldername}/${config.promotion.foldername}/${repo.name}-promote-pipeline"
      promoteJobs.add(promoteJobName)
      pipelineJob(promoteJobName) {
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
          for(credparam in project.credparams) {
            credentialsParam("${credparam.name}") {
              defaultValue("${credparam.defaultvalue}")
              description("${credparam.description}")
            }
          }
        }
      }
    }
    
    if (repo.nightly) {
      pipelineJob("${baseFolderName}/${project.foldername}/${config.nightly.foldername}/${repo.name}-nightly-pipeline") {
        description("${repo.name} nightly pipeline")
        triggers {
          cron('H 2 * * *')
        }
        definition {
          cpsScm {
            scm {
              scriptPath("JenkinsFile.Nightly")
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
          for(param in config.nightly.jobparams) {
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
  
  // Create an individual job for all Promotable Repos
  pipelineJob("${baseFolderName}/${project.foldername}/${config.promotion.foldername}/_promote-all-pipelines") {
    description("_${project.foldername} promote all pipelines")
    def masterScript = ""
    for (promoteJob in promoteJobs) {
      masterScript = masterScript + """
        build job: "${promoteJob}", wait: true
      """
    }
    definition {
      cps {
        script(masterScript)
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
      for(credparam in project.credparams) {
        credentialsParam("${credparam.name}") {
          defaultValue("${credparam.defaultvalue}")
          description("${credparam.description}")
        }
      }
    }
  }
}

// Tools Pipelines
for (repo in config.tools.repos) {
  folder("${baseFolderName}/${config.tools.foldername}") {
    displayName("venice tools pipelines")
  }
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
      for(param in config.tools.jobparams) {
        if (param.type == "booleanParam") {
        "${param.type}"("${param.name}", "${param.defaultvalue}".toBoolean(), "${param.description}")
        } else {
        "${param.type}"("${param.name}", "${param.defaultvalue}", "${param.description}")
        }
      }
      stringParam("GIT_URL", "${repo.url}", "Git repository URL")
      for(credparam in config.tools.credparams) {
        credentialsParam("${credparam.name}") {
          defaultValue("${credparam.defaultvalue}")
          description("${credparam.description}")
        }
      }
    }
  }
}