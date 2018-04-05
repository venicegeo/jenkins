#!groovy

// This is the seed job that will create the L2 jobs.
// This will mimic the behavior of manual creation of the
// manual jobs that will be created in the L4 and above environments.

String configfile = readFileFromWorkspace("seed-repos.json")
def slurper = new groovy.json.JsonSlurper()
def config = slurper.parseText(configfile)
def baseFolderName = "l2-manual-pipelines"
folder("${baseFolderName}")

// For each folder, create the repo jobs
for (folder in config.folders) {
  folder("${baseFolderName}/${folder.name}") {
    displayName("${folder.name} pipelines")
  }
  folder("${baseFolderName}/${folder.name}/promotions") {
    displayName("${folder.name} promotion jobs")
  }
  def promoteJobs = [] // Collect Promotable Jobs for a Master Promote Job
  for (repo in folder.repos) {
    pipelineJob("${baseFolderName}/${folder.name}/${repo.name}-pipeline") {
      description("${repo.name} pipeline")
      triggers {
        gitHubPushTrigger()
      }
      environmentVariables { // If the repo has specified a Threadfix ID, use that.
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
      parameters { // These are the parameters that would otherwise be manually injected.
        stringParam("GIT_URL", "${repo.url}", "Project Git repository URL")
        stringParam("CONFIGURATION_URL", "${config.configuration-url}", "Credential Git repository URL")
        stringParam("CONFIGURATION_CREDS", "${config.configuration-credentials}", "Credentials for Credential Git repository")
        stringParam("ENVIRONMENT", "${config.environment}", 
          "The environment, matching with the ENVIRONMENT-config.json file located in the configuration repository")
        if (repo.threadfixId) {
          stringParam("THREADFIX_ID", "${repo.threadfixId}", "Threadfix ID for this project")
        }
      }
    }
    if (repo.promotable) {
      def promoteJobName = "${baseFolderName}/${folder.name}/promotions/${repo.name}-promote-pipeline"
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
        parameters { // These are the parameters that would otherwise be manually injected.
          stringParam("GIT_URL", "${repo.url}", "Project Git repository URL")
          stringParam("CONFIGURATION_URL", "${config.configuration-url}", "Credential Git repository URL")
          stringParam("CONFIGURATION_CREDS", "${config.configuration-credentials}", "Credentials for Credential Git repository")
          stringParam("ENVIRONMENT", "${config.environment}", 
            "The environment, matching with the ENVIRONMENT-config.json file located in the configuration repository")
        }
      }
    }
  }
  // Create an individual job for all Promotable repos
  pipelineJob("${baseFolderName}/${folder.name}/promotions/_promote-all-pipelines") {
    description("_${folder.name} promote all pipelines")
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
  }
}